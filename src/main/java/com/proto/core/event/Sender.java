package com.proto.core.event;

import com.proto.utils.Pair;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A sender supports consuming object, translate to typed object.
 * Or consume typed object directly by-passing the translation
 *
 * @author wfrancis
 */
public abstract class Sender<T> implements Listener {

    private final Translator<T> translator;

    private final LinkedBlockingQueue<Pair<EventKey, Object>> queue;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private Thread senderThread;

    /**
     * @param translator translator to use for translating Object to T
     */
    public Sender(Translator<T> translator) {
        this.translator = translator;
        this.queue = new LinkedBlockingQueue<Pair<EventKey, Object>>();
    }

    public void consume(EventKey eventKey, Object o) {
        //event data in queue
        queue.add(new Pair(eventKey, o));
    }

    //start Tib sender thread
    public void start() throws Exception {

        // this is the running thread
        senderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (running.get()) {
                        Pair<EventKey, Object> eventData = queue.take();
                        sendEvent(eventData);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Sending of eventData failed", e);
                }
            }
        });

        // Now start everything
        running.set(true);
        senderThread.start();
    }

    //stop Tib sender thread
    public void stop() throws Exception {
        running.set(false);
    }

    //send Tib event data
    private void sendEvent(Pair<EventKey, Object> eventData) throws Exception {
        T res = translator.translate(eventData.getB());
        String key = translator.translateEventKey(eventData.getA());
        nativeConsume(key, res);
    }

    /**
     * Consume a typed object without using the translator
     * @param key
     * @param o
     */
    abstract public void nativeConsume(String key, T o) throws Exception;

}
