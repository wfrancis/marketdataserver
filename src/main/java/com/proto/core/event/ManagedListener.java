package com.proto.core.event;

import java.util.List;

/**
 * Helper interface for wiring
 * @author wfrancis
 */
public interface ManagedListener extends Listener {

    /**
     * A NOOP.
     */
    public static ManagedListener NOOP = new ManagedListener() {

        public List<EventKey> getListenerEventKeys() {
            return null;
        }

        public void consume(EventKey eventKey, Object event) {
            return;
        }
    };


    /**
     * Returns the list of EventKeys that this Listener will consume.
     * @return
     */
    public List<EventKey> getListenerEventKeys();
}
