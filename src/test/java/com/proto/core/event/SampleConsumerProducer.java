package com.proto.core.event;

import com.proto.utils.Threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class SampleConsumerProducer {

    private static SimpleChannelManager channelManager = new SimpleChannelManager(new CountingEventChannel());

    private static ManagedListener testListenerA = new ManagedListener() {
        @Override
        public List<EventKey> getListenerEventKeys() {
            List<EventKey> keys = new ArrayList<EventKey>();
            keys.add(new StringEventKey("A"));
            return keys;
        }

        @Override
        public void consume(EventKey eventKey, Object event) throws Exception {
            System.out.println("TESTLISTENER-A: EventKey: " + eventKey + " event: " +event);
        }
    };

    private static ManagedListener testListenerB = new ManagedListener() {
        @Override
        public List<EventKey> getListenerEventKeys() {
            List<EventKey> keys = new ArrayList<EventKey>();
            keys.add(new StringEventKey("B"));
            return keys;
        }

        @Override
        public void consume(EventKey eventKey, Object event) throws Exception {
            System.out.println("TESTLISTENER-B: EventKey: " + eventKey + " event: " +event);
        }
    };

    private static ManagedListener testListenerC = new ManagedListener() {
        @Override
        public List<EventKey> getListenerEventKeys() {
            List<EventKey> keys = new ArrayList<EventKey>();
            keys.add(new StringEventKey("A"));
            keys.add(new StringEventKey("B"));
            return keys;
        }

        @Override
        public void consume(EventKey eventKey, Object event) throws Exception {
            System.out.println("TESTLISTENER-C: EventKey: " + eventKey + " event: " +event);
        }
    };

    private static Producer testProducerA = new Producer() {

        private final AtomicBoolean active     = new AtomicBoolean(false);;
        private final ExecutorService executor = Executors.newSingleThreadExecutor(Threads.createFactory("SampleA"));
        private Listener listener              = null;

        private AtomicLong data                = new AtomicLong(0);

        @Override
        public void setListener(Listener listener) {
            this.listener = listener;
        }

        @Override
        public void start() throws Exception {
            active.compareAndSet(false, true);
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    while (active.get()) {
                        try {
                            listener.consume(new StringEventKey("A"), data.getAndAdd(2L));
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        @Override
        public void stop() throws Exception {
            active.compareAndSet(true, false);
            executor.shutdown();
        }
    };

    private static Producer testProducerB = new Producer() {

        private final AtomicBoolean active     = new AtomicBoolean(false);;
        private final ExecutorService executor = Executors.newSingleThreadExecutor(Threads.createFactory("SampleB"));
        private Listener listener              = null;

        private AtomicLong data                = new AtomicLong(1);

        @Override
        public void setListener(Listener listener) {
            this.listener = listener;
        }

        @Override
        public void start() throws Exception {
            active.compareAndSet(false, true);
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    while (active.get()) {
                        try {
                            listener.consume(new StringEventKey("B"), data.getAndAdd(2L));
                            TimeUnit.SECONDS.sleep(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        @Override
        public void stop() throws Exception {
            active.compareAndSet(true, false);
            executor.shutdown();
        }
    };

    public static void main(String[] args) throws Exception {
        System.out.println("(1) Set Producer's Listeners to Channel Manager");
        testProducerA.setListener(channelManager);
        testProducerB.setListener(channelManager);

        System.out.println("(2) Registering Listeners A and B Keys in Channel Manager");
        for(EventKey key : testListenerA.getListenerEventKeys()){
            channelManager.registerListener(key, testListenerA);
        }
        for(EventKey key : testListenerB.getListenerEventKeys()){
            channelManager.registerListener(key, testListenerB);
        }

        System.out.println("(3) Starting Producers A and B");
        testProducerA.start();
        testProducerB.start();

        TimeUnit.SECONDS.sleep(10);

        System.out.println("(4) Registering Listener C Keys in Channel Manager");
        for(EventKey key : testListenerC.getListenerEventKeys()){
            channelManager.registerListener(key, testListenerC);
        }

        TimeUnit.SECONDS.sleep(10);

        System.out.println("(5) UnRegistering Listener C Keys in Channel Manager");
        channelManager.unregisterManagedListener(testListenerC);

        TimeUnit.SECONDS.sleep(10);

        System.out.println("(6) UnRegistering Listener B Keys in Channel Manager");
        channelManager.unregisterManagedListener(testListenerB);

        TimeUnit.SECONDS.sleep(10);

        System.out.println("(7) UnRegistering Listener A Keys in Channel Manager");
        channelManager.unregisterManagedListener(testListenerA);

        System.out.println("(8) Stop Producers A and B");
        testProducerA.stop();
        testProducerB.stop();

        System.out.println("(9) Stop Channel Manager");
        channelManager.stop();
    }
}
