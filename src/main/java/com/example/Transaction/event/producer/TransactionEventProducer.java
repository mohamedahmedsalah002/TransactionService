package com.example.Transaction.event.producer;

import com.example.Transaction.common.constant.Topics;
import com.example.Transaction.event.model.BaseEvent;
import com.example.Transaction.event.model.TransactionCompletedEvent;
import com.example.Transaction.event.model.TransactionCreatedEvent;
import com.example.Transaction.event.model.TransactionFailedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer implements BaseProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public CompletableFuture<Void> sendEvent(String topic, BaseEvent event) {
        String key = event.getEventId() != null ? event.getEventId().toString() : "default-key";
        return sendEvent(topic, key, event);
    }

    @Override
    public CompletableFuture<Void> sendEvent(String topic, String key, BaseEvent event) {
        return CompletableFuture.runAsync(() -> {
            try {
                String json = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(topic, key, json)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to send event to topic: {} - {}", topic, ex.getMessage());
                            } else if (result != null) {
                                log.info("Event sent successfully to topic: {}, offset: {}",
                                        topic, result.getRecordMetadata().offset());
                            }
                        });
            } catch (Exception e) {
                log.error("Error sending event to topic: {}", topic, e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Publish transaction created event
     */
    public void publishTransactionCreated(TransactionCreatedEvent event) {
        try {
            log.info("Publishing TRANSACTION_CREATED event for transactionId: {}",
                    event.getData().getTransactionId());

            sendEvent(Topics.TRANSACTION_CREATED,
                    event.getData().getTransactionId().toString(),
                    event);
        } catch (Exception e) {
            log.error("Error publishing TRANSACTION_CREATED event for transactionId: {}",
                    event.getData().getTransactionId(), e);
        }
    }

    /**
     * Publish transaction completed event
     */
    public void publishTransactionCompleted(TransactionCompletedEvent event) {
        try {
            log.info("Publishing TRANSACTION_COMPLETED event for transactionId: {}",
                    event.getData().getTransactionId());

            sendEvent(Topics.TRANSACTION_COMPLETED,
                    event.getData().getTransactionId().toString(),
                    event);
        } catch (Exception e) {
            log.error("Error publishing TRANSACTION_COMPLETED event for transactionId: {}",
                    event.getData().getTransactionId(), e);
        }
    }

    /**
     * Publish transaction failed event
     */
    public void publishTransactionFailed(TransactionFailedEvent event) {
        try {
            log.info("Publishing TRANSACTION_FAILED event for transactionId: {}",
                    event.getData().getTransactionId());

            sendEvent(Topics.TRANSACTION_FAILED,
                    event.getData().getTransactionId().toString(),
                    event);
        } catch (Exception e) {
            log.error("Error publishing TRANSACTION_FAILED event for transactionId: {}",
                    event.getData().getTransactionId(), e);
        }
    }
}
