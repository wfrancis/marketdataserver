package com.proto.core.event;

import com.proto.utils.Clock;
import com.proto.utils.IClock;
import com.proto.utils.Threads;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link Producer} implementation that buffers incoming events until their appropriate time of publication.
 *
 * @author wfrancis
 */
public class BufferedProducer implements Producer, Listener {

    private static final Logger LOG = Logger.getLogger(BufferedProducer.class);

    /**
     * The default max queue time length
     */
    private static final int DEFAULT_TIMED_QUEUE_LENGTH = 100;

    /**
     * The listener interested in events
     */
    private Listener listener;

    /**
     * Controls the dispatching of events
     */
    private final IClock clock;

    /**
     * Guarding lock
     */
    private final ReentrantLock lock;

    /**
     * Signal when queue is not full, wait when queue is full.
     */
    private final Condition notFull;

    /**
     * Signal when queue becomes not empty, wait when queue is empty.
     */
    private final Condition notEmpty;

    /**
     * The event queue
     */
    private final DelayQueue<DelayedEvent> queue;

    /**
     * BufferedProducer uses executor as its thread.
     */
    private final ExecutorService executor;

    /**
     * true if the BufferedProducer is started, else false.
     */
    private final AtomicBoolean active;

    /**
     * Create an instance with the given queue size and clock
     *
     * @param clock     The clock to use to determine when events should be published.
     */
    public BufferedProducer(IClock clock) {
        this.clock = clock;
        this.lock = new ReentrantLock();
        this.notFull = lock.newCondition();
        this.notEmpty = lock.newCondition();
        this.queue = new DelayQueue<DelayedEvent>();
        this.executor = Executors.newSingleThreadExecutor(Threads.createFactory("BufferedProducer"));
        this.active = new AtomicBoolean(false);
    }

    /**
     * Create an instance with the given queue size and clock
     *
     * @param timeOffset  run option where events would start at the specified offset time
     * @param useOffset   option to use timeOffset
     * @param realTime    clock option on whether to use realtime
     */
    public BufferedProducer(String timeOffset, boolean useOffset, boolean realTime) {
        //use clock that refers to the current time OR an offset time
        if(useOffset) {
            //clock utilizing optional offset
            this.clock = new Clock(realTime, useOffset, normalizeOffsetTime(timeOffset));
        } 
        else {
            //clock without offset
            this.clock = new Clock(realTime, new Date());
        }           
        this.lock = new ReentrantLock();
        this.notFull = lock.newCondition();
        this.notEmpty = lock.newCondition();
        this.queue = new DelayQueue<DelayedEvent>();
        this.executor = Executors.newSingleThreadExecutor(Threads.createFactory("BufferedProducer"));
        this.active = new AtomicBoolean(false);
    }

    public void consume(final EventKey eventKey, final Object event) throws Exception {

        if(!active.get()) {
            throw new IllegalStateException("Producer is not active");
        }

        if (event instanceof TimedEvent) {
            lock.lock();

            try {

                final DelayedEvent newEntry  = new DelayedEvent(eventKey, (TimedEvent) event);

                // timed queue allows for all instrument events within the time window to be added to the queue
                if ((newEntry.getEvent().getTime().getTime()-clock.currentTimeMillis()) > DEFAULT_TIMED_QUEUE_LENGTH) {
                    try {
                        // wait for a maximum of 500 ms for event delay
                        // this prevents potentially waiting a long time for an illiquid symbol
                        notFull.await(DEFAULT_TIMED_QUEUE_LENGTH, TimeUnit.MILLISECONDS);

                    } catch (InterruptedException ignored) {
                        //ignored
                    }
                }

                /*
                 * in case the producer was stopped while we were waiting for space on the queue
                 */
                if(!active.get()) {
                    return;
                }
                
                final DelayedEvent nextEntry = queue.peek();

                final Long newDelay          = newEntry.getEndOfDelay();
                final Long nextDelay         = nextEntry == null ? null : nextEntry.getEndOfDelay();

                queue.put(newEntry);

                if(nextDelay == null || newDelay < nextDelay) {
                    //signal notEmpty. The new event is to be processed earlier than the current earliest event.
                    notEmpty.signalAll();
                }
            } finally {
                lock.unlock();
            }
        } else {
            throw new IllegalArgumentException("Event is not of type TimedEvent. " + event);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void start() throws Exception {
        active.compareAndSet(false, true);

        executor.submit(new Runnable() {
            public void run() {
                while (active.get()) {

                    lock.lock();
                    try {
                        DelayedEvent entry = queue.poll();

                        if (entry != null) {
                            try {
                                listener.consume(entry.getEventKey(), entry.getEvent());
                            } catch (Exception e) {
                                LOG.error("Exception caught while dispatching event", e);
                            } finally {
                                notFull.signalAll();
                            }
                        } else {
                            entry = queue.peek();
                            waitOnEmpty(entry == null ? null : entry.getEndOfDelay());
                        }
                    } finally {
                        lock.unlock();
                    }
                }

                LOG.info(Thread.currentThread().getName() + " Exiting dispatch loop");
            }
        });
    }

    public void stop() throws Exception {
        active.compareAndSet(true, false);

        lock.lock();
        try {
            queue.clear();
            notFull.signalAll();
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("AwaitNotInLoop")
    private void waitOnEmpty(Long duration) {
        try {
            if(duration == null) {
                notEmpty.await();
            } else {
                notEmpty.await(duration, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            //ignore
        }
    }

    private Date normalizeOffsetTime(String timeOffset) {

        SimpleDateFormat startTimeDataFormatter = new SimpleDateFormat("HH:mm:ss");
        Date offsetTime = null;
        try {
            offsetTime = startTimeDataFormatter.parse(timeOffset);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar now = Calendar.getInstance();

        now.setTime(new Date());

        Calendar cal = Calendar.getInstance();
        cal.setTime(offsetTime);

        cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
        cal.set(Calendar.DATE, now.get(Calendar.DATE));

        return cal.getTime();
    }

    /**
     * {@link Delayed} implementation to wrap individual events
     */
    private class DelayedEvent implements Delayed {

        private final TimedEvent event;
        private final EventKey eventKey;

        private DelayedEvent(EventKey eventKey, TimedEvent event) {
            this.eventKey = eventKey;
            this.event = event;
        }

        private long getEndOfDelay() {
            return Math.max(0, event.getTime().getTime() - clock.currentTimeMillis());
        }

        public long getDelay(TimeUnit unit) {
            return unit.convert(getEndOfDelay(), TimeUnit.MILLISECONDS);
        }

        public int compareTo(Delayed o) {
            return event.getTime().compareTo(((DelayedEvent) o).event.getTime());
        }

        public TimedEvent getEvent() {
            return event;
        }

        public EventKey getEventKey() {
            return eventKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final DelayedEvent that = (DelayedEvent) o;

            return getEndOfDelay() == getEndOfDelay() &&
                    !(event != null ? !event.equals(that.event) : that.event != null);
        }

        @Override
        public int hashCode() {
            long endOfDelay = getEndOfDelay();

            int result = event != null ? event.hashCode() : 0;
            result = 31 * result + (int) (endOfDelay ^ (endOfDelay >>> 32));
            return result;
        }
    }
}
