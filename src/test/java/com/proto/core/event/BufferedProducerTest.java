package com.proto.core.event;

import com.proto.utils.Dates;
import com.proto.utils.SettableClock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the {@link BufferedProducer} class
 *
 * @author wfrancis
 */
public class BufferedProducerTest {

    private BufferedProducer producer;
    private SettableClock clock;
    private Date base = Dates.getDayAtMidnight(new Date());

    private List<TimedEvent> events;
    private CountDownLatch latch;

    private final Integer eventCount = 4;

    @Mock
    private EventKey key;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        events   = Collections.synchronizedList(new ArrayList<TimedEvent>());
        latch    = new CountDownLatch(eventCount);
        clock    = new SettableClock();
        producer = new BufferedProducer(clock);

        clock.setTime(base);
        producer.setListener(new Listener() {
            public void consume(EventKey eventKey, Object event) throws Exception {
                assertFalse(event == null);
                assertTrue(event instanceof TimedEvent);

                events.add((TimedEvent) event);

                latch.countDown();
            }
        });
    }

    @After
    public void tearDown() {
        if(producer != null) {
            try {
                producer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConsumeBadEvent() throws Exception {
        producer.start();
        producer.consume(mock(EventKey.class), new Object());
    }

    @Test(expected = IllegalStateException.class)
    public void testConsumeWhenNotActive() throws Exception {
        producer.consume(mock(EventKey.class), new Object());
    }

    @Test(expected = IllegalStateException.class)
    public void testConsumeAfterStop() throws Exception {
        producer.start();
        producer.stop();
        producer.consume(mock(EventKey.class), new Object());
    }

    @Test
    public void testConsume() throws Exception {
        producer.start();

        final DummyTestEvent event1 = new DummyTestEvent(Dates.addSeconds(base, 9));
        final DummyTestEvent event2 = new DummyTestEvent(Dates.addSeconds(base, 10));
        final DummyTestEvent event3 = new DummyTestEvent(Dates.addSeconds(base, 11));
        final DummyTestEvent event4 = new DummyTestEvent(Dates.addSeconds(base, 12));

        final CountDownLatch publishLatch = new CountDownLatch(1);

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            public void run() {
                try {
                    producer.consume(key, event2);
                    producer.consume(key, event3);
                    producer.consume(key, event1);

                    publishLatch.countDown();

                    /*
                     * The following should block until the first event is published
                     */
                    producer.consume(key, event4);
                } catch (Exception e) {
                    fail("Exception caught: " + e.getMessage());
                }
            }
        });

        publishLatch.await();

        assertTrue("Events were published before their appropriate time", events.isEmpty());

        for(int i = 1; i < 9; i++) {
            clock.setTime(Dates.addSeconds(base, i));
            assertTrue("Events were published before their appropriate time", events.isEmpty());
        }

        clock.setTime(Dates.addSeconds(base, 9));
        clock.setTime(Dates.addSeconds(base, 10));
        clock.setTime(Dates.addSeconds(base, 11));
        clock.setTime(Dates.addSeconds(base, 12));

        latch.await();

        assertTrue("Event count not as expected", events.size() == eventCount);

        for(int i = 0; i < eventCount - 1; i++) {
            assertTrue(
                String.format("Expected event time %s does not match actual %s for index %s",
                        events.get(i).getTime(),
                        Dates.addSeconds(base, 9 + i),
                        i
                ),
                events.get(i).getTime().equals(Dates.addSeconds(base, 9 + i))
            );
        }
    }

    @Test
    public void testStopWhileBlocking() throws Exception {
        producer.start();

        final DummyTestEvent event1 = new DummyTestEvent(Dates.addSeconds(base, 9));
        final DummyTestEvent event2 = new DummyTestEvent(Dates.addSeconds(base, 10));
        final DummyTestEvent event3 = new DummyTestEvent(Dates.addSeconds(base, 11));
        final DummyTestEvent event4 = new DummyTestEvent(Dates.addSeconds(base, 12));

        final CountDownLatch publishLatch = new CountDownLatch(1);
        final CountDownLatch publishLatch2 = new CountDownLatch(1);

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            public void run() {
                try {
                    producer.consume(key, event2);
                    producer.consume(key, event3);
                    producer.consume(key, event1);

                    publishLatch.countDown();

                    /*
                     * The following should block until the first event is published
                     */
                    producer.consume(key, event4);

                    publishLatch2.countDown();
                } catch (Exception e) {
                    fail("Exception caught: " + e.getMessage());
                }
            }
        });

        publishLatch.await();

        assertTrue("Events were published before their appropriate time", events.isEmpty());

        for(int i = 1; i < 9; i++) {
            clock.setTime(Dates.addSeconds(base, i));
            assertTrue("Events were published before their appropriate time", events.isEmpty());
        }

        producer.stop();

        publishLatch2.await();

        assertTrue("Event count not as expected", events.size() == 0);
    }

    @Test
    public void testConsumeAfterException() throws Exception {
        final Listener listener = mock(Listener.class);

        doThrow(new Exception("Some exception")).when(listener).consume(eq(key), any(TimedEvent.class));

        producer.setListener(listener);
        producer.start();

        final DummyTestEvent event1 = new DummyTestEvent(base);
        final DummyTestEvent event2 = new DummyTestEvent(base);

        producer.consume(key, event1);

        reset(listener);

        producer.consume(key, event2);

        Thread.sleep(1000);

        verify(listener).consume(eq(key), eq(event2));
    }

    /**
     * Utility class used for testing
     */
    class DummyTestEvent implements TimedEvent {
        private Date date;

        DummyTestEvent(Date date) {
            this.date = date;
        }

        public Date getTime() {
            return date;
        }
    }
}
