package com.example.Transaction.event.consumer;

import com.example.Transaction.common.constant.Topics;
import com.example.Transaction.event.model.BaseEvent;
import com.example.Transaction.event.model.TransactionCompletedEvent;
import com.example.Transaction.event.model.TransactionCreatedEvent;
import com.example.Transaction.event.model.TransactionFailedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes transaction events published by this service (listen to yourself).
 * Uses a separate consumer group so it receives all transaction events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer implements BaseConsumer {

    private static final String SELF_CONSUMER_GROUP = "transaction-internal-group";

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = Topics.TRANSACTION_CREATED, groupId = SELF_CONSUMER_GROUP)
    public void consumeTransactionCreatedEvent(String message) {
        try {
            TransactionCreatedEvent event = objectMapper.readValue(message, TransactionCreatedEvent.class);
            log.info("Received TRANSACTION_CREATED event (self): {}", event.getEventId());
            processEvent(event);
        } catch (Exception e) {
            log.error("Failed to process TRANSACTION_CREATED event", e);
            try {
                TransactionCreatedEvent event = objectMapper.readValue(message, TransactionCreatedEvent.class);
                handleError(event, e);
            } catch (Exception ex) {
                log.error("Could not deserialize event for error handling", ex);
            }
        }
    }

    @KafkaListener(topics = Topics.TRANSACTION_COMPLETED, groupId = SELF_CONSUMER_GROUP)
    public void consumeTransactionCompletedEvent(String message) {
        try {
            TransactionCompletedEvent event = objectMapper.readValue(message, TransactionCompletedEvent.class);
            log.info("Received TRANSACTION_COMPLETED event (self): {}", event.getEventId());
            processEvent(event);
        } catch (Exception e) {
            log.error("Failed to process TRANSACTION_COMPLETED event", e);
            try {
                TransactionCompletedEvent event = objectMapper.readValue(message, TransactionCompletedEvent.class);
                handleError(event, e);
            } catch (Exception ex) {
                log.error("Could not deserialize event for error handling", ex);
            }
        }
    }

    @KafkaListener(topics = Topics.TRANSACTION_FAILED, groupId = SELF_CONSUMER_GROUP)
    public void consumeTransactionFailedEvent(String message) {
        try {
            TransactionFailedEvent event = objectMapper.readValue(message, TransactionFailedEvent.class);
            log.info("Received TRANSACTION_FAILED event (self): {}", event.getEventId());
            processEvent(event);
        } catch (Exception e) {
            log.error("Failed to process TRANSACTION_FAILED event", e);
            try {
                TransactionFailedEvent event = objectMapper.readValue(message, TransactionFailedEvent.class);
                handleError(event, e);
            } catch (Exception ex) {
                log.error("Could not deserialize event for error handling", ex);
            }
        }
    }

    @Override
    public void processEvent(BaseEvent event) {
        log.info("Processing transaction event - Type: {}, ID: {}, Timestamp: {}",
                event.getEventType(), event.getEventId(), event.getTimestamp());

        if (event instanceof TransactionCreatedEvent createdEvent) {
            handleTransactionCreated(createdEvent);
        } else if (event instanceof TransactionCompletedEvent completedEvent) {
            handleTransactionCompleted(completedEvent);
        } else if (event instanceof TransactionFailedEvent failedEvent) {
            handleTransactionFailed(failedEvent);
        } else {
            log.warn("Unknown transaction event type: {}", event.getEventType());
        }
    }

    private void handleTransactionCreated(TransactionCreatedEvent event) {
        TransactionCreatedEvent.TransactionEventData data = event.getData();

        log.info("=== Transaction Created (self) ===");
        log.info("Event ID: {}", event.getEventId());
        log.info("Service: {}", event.getServiceName());
        log.info("Transaction ID: {}", data.getTransactionId());
        log.info("From Account: {}, To Account: {}", data.getFromAccount(), data.getToAccount());
        log.info("Amount: {}, Type: {}, Status: {}", data.getAmount(), data.getTransactionType(), data.getStatus());
        log.info("Timestamp: {}", event.getTimestamp());
        log.info("==================================");

        // TODO: Add business logic - e.g. analytics, notifications, audit log, cache invalidation
        log.info("Successfully processed TRANSACTION_CREATED (self) for transactionId: {}", data.getTransactionId());
    }

    private void handleTransactionCompleted(TransactionCompletedEvent event) {
        TransactionCompletedEvent.TransactionEventData data = event.getData();

        log.info("=== Transaction Completed (self) ===");
        log.info("Event ID: {}", event.getEventId());
        log.info("Service: {}", event.getServiceName());
        log.info("Transaction ID: {}", data.getTransactionId());
        log.info("From Account: {}, To Account: {}", data.getFromAccount(), data.getToAccount());
        log.info("Amount: {}, Type: {}", data.getAmount(), data.getTransactionType());
        log.info("Completed At: {}", data.getCompletedAt());
        log.info("====================================");

        // TODO: Add business logic - e.g. update balances, send receipt, trigger workflows
        log.info("Successfully processed TRANSACTION_COMPLETED (self) for transactionId: {}", data.getTransactionId());
    }

    private void handleTransactionFailed(TransactionFailedEvent event) {
        TransactionFailedEvent.TransactionFailureData data = event.getData();

        log.info("=== Transaction Failed (self) ===");
        log.info("Event ID: {}", event.getEventId());
        log.info("Service: {}", event.getServiceName());
        log.info("Transaction ID: {}", data.getTransactionId());
        log.info("From Account: {}, To Account: {}", data.getFromAccount(), data.getToAccount());
        log.info("Amount: {}, Type: {}", data.getAmount(), data.getTransactionType());
        log.info("Failure Reason: {}", data.getFailureReason());
        log.info("Failed At: {}", data.getFailedAt());
        log.info("=================================");

        // TODO: Add business logic - e.g. alert, retry queue, notify user
        log.info("Successfully processed TRANSACTION_FAILED (self) for transactionId: {}", data.getTransactionId());
    }

    @Override
    public void handleError(BaseEvent event, Throwable throwable) {
        log.error("Error processing transaction event");
        log.error("Event Type: {}", event.getEventType());
        log.error("Event ID: {}", event.getEventId());
        log.error("Service Name: {}", event.getServiceName());
        log.error("Timestamp: {}", event.getTimestamp());
        log.error("Error: {}", throwable.getMessage(), throwable);
        log.warn("Event processing failed - manual intervention may be required");
    }

    @Override
    public boolean canHandle(String eventType) {
        return eventType != null && eventType.startsWith("TRANSACTION_");
    }
}
