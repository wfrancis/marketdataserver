package com.proto.core.event;

/**
 * Event sent to Listener class to control its behavior, e.g. subscription, unsubscription, etc.
 * .
 * @author wfrancis
 */
public class ControlEvent {

    /**
     * Use of EventKey in ControlEvent allows use of a single control channel per multiple data channels.
     */
    EventKey key;
    Listener requester;

    public ControlEvent() {
        key = null;
        requester = null;
    }

    public ControlEvent(EventKey key, Listener requester) {
        this.key = key;
        this.requester = requester;
    }

    public Listener getRequester() {
        return requester;
    }

    public EventKey getKey() {
        return key;
    }

    public ReturnValue setRequester(Listener requester) {
        this.requester = requester;
        return ReturnValue.OK;
    }

    public boolean matches(ControlEvent other) {
        return this == other;
    }
}
