package com.proto.core.event;

/**
 * Factory of channel
 *
 * @author wfrancis
 */
public interface ChannelFactory {

    /**
     * Allocate a channel
     */
    Channel allocate();
}
