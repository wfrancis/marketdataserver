package com.proto.core.data.simulated;

import com.proto.core.event.Listener;
import com.proto.core.event.Producer;
import com.proto.utils.Threads;
import org.apache.log4j.Logger;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class FakeFXMarketDataFeed implements Producer {

    private static final Logger LOG = Logger.getLogger(FakeFXMarketDataFeed.class);

    /**
     * The listener interested in events
     */
    private Listener listener;

    /**
     * Init instrument price
     */
    private double initInstrumentPx = 100.0;

    /**
     * Init px tick rate (milliseconds)
     */
    private long initPxTickRate = 10;

    /**
     * Px delay time random seed
     */
    private long delayTimeSeed = 1;

    /**
     * Px tick movement random seed
     */
    private long priceRandomWalkSeed = 1;

    /**
     * BufferedProducer uses executor as its thread.
     */
    private final ExecutorService executor;

    /**
     * true if the BufferedProducer is started, else false.
     */
    private final AtomicBoolean active;

    /**
     * Guarding lock
     */
    private final ReentrantLock lock;

    public FakeFXMarketDataFeed(Properties config) {
        this.executor = Executors.newSingleThreadExecutor(Threads.createFactory("FakeFXMarketDataFeed"));
        this.active = new AtomicBoolean(false);
        this.lock = new ReentrantLock();

        if(config.contains("DELAY_TIME_SEED")) {
            this.delayTimeSeed = Long.parseLong(config.getProperty("DELAY_TIME_SEED"));
        }
        if(config.contains("PRICE_RANDOM_WALK_SEED")) {
            this.priceRandomWalkSeed = Long.parseLong(config.getProperty("PRICE_RANDOM_WALK_SEED"));
        }
        if(config.contains("INIT_PX")) {
            this.initInstrumentPx = Double.parseDouble(config.getProperty("INIT_PX"));
        }

        if(config.contains("INIT_TICK_RATE")) {
            this.initPxTickRate = Long.parseLong(config.getProperty("INIT_TICK_RATE"));
        }
    }

    @Override
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

//                        BufferedProducer.DelayedEvent entry = queue.poll();
//
//                        if (entry != null) {
//                            try {
//                                listener.consume(entry.getEventKey(), entry.getEvent());
//                            } catch (Exception e) {
//                                LOG.error("Exception caught while dispatching event", e);
//                            } finally {
//                                notFull.signalAll();
//                            }
//                        } else {
//                            entry = queue.peek();
//                            waitOnEmpty(entry == null ? null : entry.getEndOfDelay());
//                        }

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

//            queue.clear();
//            notFull.signalAll();
//            notEmpty.signalAll();

        } finally {
            lock.unlock();
        }
    }
}
