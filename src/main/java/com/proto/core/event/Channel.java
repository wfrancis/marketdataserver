package com.proto.core.event;

/**
 * Define a channel that represents a subscription of {@link Object}
 * Channel is responsible for forwarding appropriate Events to Listener,
 *  and for forwarding appropriate ControlEvents to Listener.
 * Channels are not thread-safe unless explicitly stated.
 *
 * @author wfrancis
 */
public interface Channel extends Listener {

    /**
     * If the Listener is not in Channel, then add
     * @param listener
     * @return if producer is already in Channel then ReturnValue.IGNORED, else ReturnValue.OK.
     */

    public ReturnValue registerListener(Listener listener);
    /**
     * If the Listener exists in Channel, then remove
     * @param listener
     * @return if producer is not found then ReturnValue.IGNORED, else ReturnValue.OK.
     */
    public ReturnValue unregisterListener(Listener listener);

    /**
     * Set the manager responsible for this channel.
     * @param manager
     */
    public void setManager(Listener manager);
}
