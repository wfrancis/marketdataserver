package com.proto.core.event;

/**
 * Event sent to Listener class to indicate a new subscription.
 * Note that some ChannelManager classes will act as a filter, i.e. remove redundant ControlEvents from reaching a Listener.
 *
 * @author wfrancis
 */
public class SubscriptionControlEvent extends ControlEvent {
    public SubscriptionControlEvent(EventKey key, Listener requester) {
        super(key, requester);
    }
}
