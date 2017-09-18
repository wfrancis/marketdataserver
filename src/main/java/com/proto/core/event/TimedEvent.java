package com.proto.core.event;

import java.util.Date;

/**
 * Marker interface for events that have a time associated with them.
 *
 * @author wfrancis
 */
public interface TimedEvent {
    Date getTime();
}
