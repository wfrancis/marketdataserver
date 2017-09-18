package com.proto.core.event;

/**
 * Utility interface that unifies the producer and managed listener
 * @author wfrancis
 */
public interface ManagedProducer extends Producer, ManagedListener {
}
