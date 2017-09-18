package com.proto.core.event;

import java.util.HashSet;

/**
 * A simple event channel
 *
 * @author wfrancis
 */
public class SimpleEventChannel implements Channel, ChannelFactory {

    private final HashSet<Listener> registeredConsumers = new HashSet<Listener>();
    protected Listener manager;

    protected void sendControlEvent(EventKey eventKey, ControlEvent ce) throws Exception{
        if (registeredConsumers.isEmpty())
            return;
        for(Listener consumer : registeredConsumers) {
            consumer.consume(eventKey, ce);
        }
    }

    /**
     * Subscribe.
     * @param ce
     * @return  Duplicate subscriptions will be ignored.
     */
    protected void subscribe(EventKey eventKey, SubscriptionControlEvent ce) throws Exception {
           ce.setRequester(this);
           sendControlEvent(eventKey, ce);
    }

    protected void unsubscribe(EventKey eventKey, UnsubscriptionControlEvent ce) throws Exception {
           ce.setRequester(this);
           sendControlEvent(eventKey, ce);
    }

    public ReturnValue unregisterListener(Listener producer) {
        if (!registeredConsumers.contains(producer))
            return ReturnValue.IGNORED;
        else {
            registeredConsumers.remove(producer);
            return ReturnValue.OK;
        }
    }

    public Channel allocate() {
        return new SimpleEventChannel();
    }

    public ReturnValue registerListener(Listener producer) {
        if (registeredConsumers.contains(producer))
            return ReturnValue.IGNORED;
        else {
            registeredConsumers.add(producer);
            return ReturnValue.OK;
        }
    }

    public void consume(EventKey eventKey, final Object event) throws Exception {
        if (event instanceof ControlEvent) {
            //Optimize for the scenario where event is a regular data. Check for ControlEvent first so in the case of regular data we only check instanceof once.
            if (event instanceof SubscriptionControlEvent) {
                subscribe(eventKey, (SubscriptionControlEvent) event);
            }
            else if (event instanceof UnsubscriptionControlEvent) {
                unsubscribe(eventKey, (UnsubscriptionControlEvent) event);
            }
        }
        else {
            for(Listener consumer : registeredConsumers) {
                consumer.consume(eventKey, event);
            }
        }
    }

    public void setManager(Listener manager) {
        this.manager = manager;
    }
}
