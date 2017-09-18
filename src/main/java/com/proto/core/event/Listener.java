package com.proto.core.event;

/**
 * Defines a consumer of certain {@link Object}.
 *
 * @author wfrancis
 */
public interface Listener {

    /**
     * @param eventKey subject of object.
     * @param event event to be consumed
     * @return
     */
    void consume(EventKey eventKey, Object event) throws Exception;
}
