package com.proto.core.event;

/**
 * Channel Manager manages a EventKey based subscription relation by {@link EventKey}
 *
 * We do not have type safety check here, we can handle
 *
 * @author wfrancis
 */
public interface ChannelManager extends Listener {

    /**
     * Register a listener for the data that the listener is interested in receiving
     *
     * @return
     */
    void registerListener(final EventKey eventKey, final Listener listener);

    /**
     * Unregister a listener so that it will no longer receive data.
     *
     * @return
     */
    void unregisterListener(final EventKey eventKey, final Listener listener);

    void stop() throws Exception;
}
