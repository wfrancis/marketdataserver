package com.proto.core.event;;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A manager for different event channel of the client simulation. Run everything on a single thread
 * without blocking the producer thread.
 *
 * SimpleChannelManager is thread-safe.
 * It has limited support for wild card EventKeys.
 * Wild card will override any specific value.
 *  i.e. FOO*RANDOMSTRING is same as FOO*
 * Wild cards are only supported for registration/subscription.
 * Any producer of data must specify a full EventKey with no wildcards.
 * Unsubscriptions and registration must be on EventKeys that exactly match the original subscriptions and registration.
 *
 * @author wfrancis
 */
public class SimpleChannelManager implements ManagedChannelManager {
    
    private static final Logger LOG = Logger.getLogger(SimpleChannelManager.class);

    /**
     * Factory for the channel
     */
    protected final ChannelFactory channelFactory;

    /**
     * Wildcard registration
     */
    private final NavigableMap<String, List<Listener>> wRegistrations = new TreeMap<String, List<Listener>>();

    /**
     * Channels for each event key
     */
    private final NavigableMap<String, Channel> channels = new TreeMap<String, Channel>();

    /**
     * executor service to run everything on
     */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * @param channelFactory factory used to create new channels
     */
    public SimpleChannelManager(ChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }

    public void registerListener(final EventKey eventKey, final Listener listener) {
        executorService.submit(new Runnable() {

            @Override
            public void run() {
                String eventKeyString = eventKey.toString();
                String wildCardRoot = extractWildRoot(eventKeyString);

                if(wildCardRoot == null) {
                    Channel channel = getChannel(eventKeyString);
                    register(channel, listener);
                }
                else {
                    for(Channel channel : getChannels(wildCardRoot)) {
                        register(channel, listener);
                    }
                    getWildcardRegistration(wildCardRoot).add(listener);
                }
            }

        });
    }

    public void unregisterListener(final EventKey eventKey, final Listener listener) {
        executorService.submit(new Runnable() {

            @Override
            public void run() {
                String eventKeyString = eventKey.toString();
                String wildCardRoot = extractWildRoot(eventKeyString);

                if(wildCardRoot == null) {
                    Channel channel = getChannel(eventKeyString);
                    unregister(channel, listener);
                }
                else {
                    for(Channel channel : getChannels(wildCardRoot)) {
                        unregister(channel, listener);
                    }
                    getWildcardRegistration(wildCardRoot).remove(listener);
                }
            }

        });
    }

    /**
     * @throws IllegalArgumentException if eventKey is a wildcard key determined by extractWildRoot()
     */
    public void consume(final EventKey eventKey, final Object event) throws Exception {

        if (SimpleChannelManager.extractWildRoot(eventKey.toString()) != null) {
            throw new IllegalArgumentException("Wildcard EventKeys are not supported when publishing.");
        }

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    String eventKeyString = eventKey.toString();
                    Channel channel = getChannel(eventKeyString);
                    channel.consume(eventKey, event);

                } catch (Exception e) {
                    LOG.error("Error consume, eventKey: " + eventKey + ", event: " + event + ".", e);
                }
            }
        });
    }

    public void unregisterManagedListener(ManagedListener mListener) {
        List<EventKey> keys = mListener.getListenerEventKeys();
        if (keys == null) {
            return;
        }
        for (EventKey eventKey : keys) {
            unregisterListener(eventKey, mListener);
        }
    }

    public void registerManagedListener(ManagedListener mListener) {
        List<EventKey> keys = mListener.getListenerEventKeys();
        if (keys == null) {
            return;
        }
        for (EventKey eventKey : keys) {
            registerListener(eventKey, mListener);
        }
    }

    /**
     * Extract the wild card root from a string, return null if
     * the string is not a wild card key string. Ex: "abc>" => "abc"
     */
    protected static String extractWildRoot(String eventKeyString) {
        int wildCardIndex = eventKeyString.indexOf(EventKey.wildChar);
        return wildCardIndex >=0 ? eventKeyString.substring(0, wildCardIndex) : null;
    }

    /**
     * Get a channel for a regular event key. Create if there isn't one existing.
     * When creating a channel, search the wildcard registered listeners and include
     * them into the channel if they matches the event key.
     */
    private Channel getChannel(String regularEventKey) {
        Channel channel = channels.get(regularEventKey);
        if(channel == null) {
            channel = channelFactory.allocate();
            includeWildcardRegistration(channel, regularEventKey);
            channels.put(regularEventKey, channel);
        }
        return channel;
    }

    /**
     * Get a list of channels for a wildcard event key root.
     */
    private List<Channel> getChannels(String wildCardRoot) {
        List<Channel> res = new ArrayList<Channel>();
        Entry<String, Channel> higher = channels.higherEntry(wildCardRoot);
        while(higher != null && higher.getKey().startsWith(wildCardRoot)) {
            res.add(higher.getValue());
            higher = channels.higherEntry(higher.getKey());
        }
        return res;
    }

    /**
     * Get a list of listeners who registered for specific wild card root.
     */
    private List<Listener> getWildcardRegistration(String wildCardRoot) {
        List<Listener> listeners = wRegistrations.get(wildCardRoot);
        if(listeners == null) {
            listeners = new ArrayList<Listener>();
            wRegistrations.put(wildCardRoot, listeners);
        }
        return listeners;
    }

    /**
     * Search in the wild card registration records, register them to the channel if
     * they match a specific event key string
     */
    private void includeWildcardRegistration(Channel channel, String regularEventKey) {

        // Search higher
        Entry<String, List<Listener>> higher = wRegistrations.higherEntry(regularEventKey);
        while(higher != null && regularEventKey.startsWith(higher.getKey())) {
            for(Listener listener : higher.getValue()) {
                register(channel, listener);
            }
            higher = wRegistrations.higherEntry(higher.getKey());
        }

        // Search lower
        Entry<String, List<Listener>> lower = wRegistrations.lowerEntry(regularEventKey);
        while(lower != null && regularEventKey.startsWith(lower.getKey())) {
            for(Listener listener : lower.getValue()) {
                register(channel, listener);
            }
            lower = wRegistrations.lowerEntry(lower.getKey());
        }
    }

    /**
     * Register a listener to a channel
     */
    private void register(Channel channel, Listener listener) {
        channel.setManager(this);
        channel.registerListener(listener);
    }

    /**
     * Ungister a listener to a channel
     */
    private void unregister(Channel channel, Listener listener) {
        channel.setManager(null);
        channel.unregisterListener(listener);
    }

    public void stop() throws Exception {
        executorService.shutdown();
    }

}
