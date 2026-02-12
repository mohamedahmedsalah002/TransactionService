package com.example.Transaction.event.consumer;

import com.example.Transaction.event.model.BaseEvent;

/**
 * Base interface for all Kafka event consumers.
 * Implementations process events received from Kafka topics.
 */
public interface BaseConsumer {

    /**
     * Process the received event.
     */
    void processEvent(BaseEvent event);

    /**
     * Handle errors that occur during event processing.
     */
    void handleError(BaseEvent event, Throwable throwable);

    /**
     * Check if this consumer can handle the given event type.
     */
    boolean canHandle(String eventType);
}
