package com.example.Transaction.event.producer;

import com.example.Transaction.event.model.BaseEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Base interface for all Kafka event producers.
 * Implementations publish events to Kafka topics.
 */
public interface BaseProducer {

    /**
     * Send event to the given topic (key will be eventId).
     */
    CompletableFuture<Void> sendEvent(String topic, BaseEvent event);

    /**
     * Send event to the given topic with custom key.
     */
    CompletableFuture<Void> sendEvent(String topic, String key, BaseEvent event);
}
