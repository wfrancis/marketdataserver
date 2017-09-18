package com.proto.core.event;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wfrancis
 */
public class CountingEventChannel extends SimpleEventChannel {
    HashMap<String, AtomicInteger> subscriptionCountMap = new HashMap<String, AtomicInteger>();



    @Override
    protected void subscribe(EventKey eventKey, SubscriptionControlEvent ce) throws Exception {
        AtomicInteger subscriptionCount = subscriptionCountMap.get(eventKey.toString());
        if (subscriptionCount == null) {
            subscriptionCount = new AtomicInteger(0);
            subscriptionCountMap.put(eventKey.toString(), subscriptionCount);
        }

        subscriptionCount.getAndIncrement();

        //only subscribe for the initial subscription
        if (subscriptionCount.get() == 1) {
            super.subscribe(eventKey, ce);
        }
        else { //subscription already exists.  Do nothing else.
        }
    }

    @Override
    protected void unsubscribe(EventKey eventKey, UnsubscriptionControlEvent ce) throws Exception {
        AtomicInteger subscriptionCount = subscriptionCountMap.get(eventKey.toString());
        if (subscriptionCount == null) {
            return;
        }

        if (subscriptionCount.get() > 0) {
            subscriptionCount.decrementAndGet();
            if (subscriptionCount.get() > 0) {
                return; //Return at this point since subscription is still alive.
            }
            else { //unsubscribe from producer since there are no more clients interested.
                super.unsubscribe(eventKey, ce);
            }
        }
        else {
            return; //Ignored since there is no subscription for this Client.
        }
    }

    @Override
    public Channel allocate() {
        return new CountingEventChannel();
    }
}
