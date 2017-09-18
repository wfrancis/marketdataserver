package com.proto.core.event;

/**
 * Support register/unregister {@link ManagedListener}
 *
 * @author wfrancis
 */
public interface ManagedChannelManager extends ChannelManager {

    /**
     * Helper function. Registers ManagedListener for this ChannelManager.
     *
     * @param mListener
     */
    public void registerManagedListener(ManagedListener mListener);

    /**
     * Helper function. Unregisters ManagedListener for this ChannelManager.
     *
     * @param mListener
     */
    public void unregisterManagedListener(ManagedListener mListener);
}
