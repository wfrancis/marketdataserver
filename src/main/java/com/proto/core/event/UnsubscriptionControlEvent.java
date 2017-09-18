package com.proto.core.event;

/**
 * Event sent to Listener class to indicate an unsubscription.
 * Note that some ChannelManager classes will act as a filter, i.e. remove redundant ControlEvents from reaching a Listener
 * @author wfrancis
 */
public class UnsubscriptionControlEvent extends ControlEvent {
      public UnsubscriptionControlEvent(EventKey key, Listener requester) {
        super(key, requester);
    }
}
