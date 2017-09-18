package com.proto.core.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A receiver support receiving typed object, and translated to object that consumed by one listener.
 *
 * @author wfrancis
 */
public abstract class Receiver<T> implements Producer {

    private final Translator<T> translator;

    private final AtomicReference<Listener> listenerRef = new AtomicReference<Listener>();

    /**
     * Executor to run receiver
     * This brings the tib message into the application     
     */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Receiver(Translator<T> translator) {
        this.translator = translator;
    }

    @Override
    public void setListener(Listener listener) {
        this.listenerRef.set(listener);
    }

    public void receive(final String key, final T o) throws Exception {

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                EventKey eventKey = null;
                Object untranslated = null;
                try {
                    eventKey = translator.untranslateEventKey(key);
                    untranslated = translator.untranslate(o);

                    Listener listener = listenerRef.get();
                    if(listener != null) {
                        listener.consume(eventKey, untranslated);
                    }
                } catch (Translator.TranslationException e) {
                    throw new RuntimeException("Error un-translating, eventKey: " + key + ", event: " + o + ".", e);
                } catch (Exception e) {
                    throw new RuntimeException("Error consuming, eventKey: " + key + ", event: " + o + ".", e);
                }
            }
        });

    }

}
