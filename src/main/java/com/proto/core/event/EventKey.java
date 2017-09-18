package com.proto.core.event;

/**
 * Defines a key which should be attached to an event.
 *
 * @author wfrancis
 */
public interface EventKey {

    public static final String wildChar = ">";
    public static final String stringDelimiter= ".";
    /**
     * Check if the current key matches another key
     * @param other other key
     * @return if the current key matches another key
     */
    boolean matches(EventKey other);

    /**
     * Accessor to namespace. As a reminder that this is a required field in EventKey.
     *
     * @return
     */
    public String getNamespace();
}
