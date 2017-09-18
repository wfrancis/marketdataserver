package com.proto.core.event;

/**
 * Helper interface for wiring.
 * The classes of this interface are not necessarily thread-safe.
 * @author wfrancis
 */
public interface Producer {

    /**
     * Sets the target that will consume the output of this Producer.
     * @param listener
     */
    void setListener(Listener listener);

    /**
     * Implementation of start() must be non-blocking and return.
     * @throws Exception
     */
    public void start() throws Exception;
    public void stop() throws Exception;
}
