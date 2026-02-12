package com.example.Transaction.event.consumer;

import com.example.Transaction.event.model.BaseEvent;
import com.example.Transaction.event.model.CreateUserEvent;
import com.example.Transaction.event.model.UserDeletedEvent;
import com.example.Transaction.event.model.UserUpdatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer implements BaseConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user.created", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserCreatedEvent(String message) {
        try {
            CreateUserEvent event = objectMapper.readValue(message, CreateUserEvent.class);
            log.info("Received USER_CREATED event from VirtualBank: {}", event.getEventId());
            processEvent(event);
        } catch (Exception e) {
            log.error("Failed to process USER_CREATED event", e);
            try {
                CreateUserEvent event = objectMapper.readValue(message, CreateUserEvent.class);
                handleError(event, e);
            } catch (Exception ex) {
                log.error("Could not deserialize event for error handling", ex);
            }
        }
    }

    @KafkaListener(topics = "user.updated", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserUpdatedEvent(String message) {
        try {
            UserUpdatedEvent event = objectMapper.readValue(message, UserUpdatedEvent.class);
            log.info("Received USER_UPDATED event from VirtualBank: {}", event.getEventId());
            processEvent(event);
        } catch (Exception e) {
            log.error("Failed to process USER_UPDATED event", e);
            try {
                UserUpdatedEvent event = objectMapper.readValue(message, UserUpdatedEvent.class);
                handleError(event, e);
            } catch (Exception ex) {
                log.error("Could not deserialize event for error handling", ex);
            }
        }
    }

    @KafkaListener(topics = "user.deleted", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserDeletedEvent(String message) {
        try {
            UserDeletedEvent event = objectMapper.readValue(message, UserDeletedEvent.class);
            log.info("Received USER_DELETED event from VirtualBank: {}", event.getEventId());
            processEvent(event);
        } catch (Exception e) {
            log.error("Failed to process USER_DELETED event", e);
            try {
                UserDeletedEvent event = objectMapper.readValue(message, UserDeletedEvent.class);
                handleError(event, e);
            } catch (Exception ex) {
                log.error("Could not deserialize event for error handling", ex);
            }
        }
    }

    @Override
    public void processEvent(BaseEvent event) {
        log.info("Processing user event - Type: {}, ID: {}, Timestamp: {}",
                event.getEventType(), event.getEventId(), event.getTimestamp());

        if (event instanceof CreateUserEvent createEvent) {
            handleUserCreated(createEvent);
        } else if (event instanceof UserUpdatedEvent updateEvent) {
            handleUserUpdated(updateEvent);
        } else if (event instanceof UserDeletedEvent deleteEvent) {
            handleUserDeleted(deleteEvent);
        } else {
            log.warn("Unknown user event type: {}", event.getEventType());
        }
    }

    private void handleUserCreated(CreateUserEvent event) {
        CreateUserEvent.UserEventData data = event.getData();

        log.info("=== User Created Event (from VirtualBank) ===");
        log.info("Event ID: {}", event.getEventId());
        log.info("Service: {}", event.getServiceName());
        log.info("User ID: {}, Username: {}, Email: {}", data.getUserId(), data.getUsername(), data.getEmail());
        log.info("Timestamp: {}", event.getTimestamp());
        log.info("=============================================");

        // TODO: Cache user, create local record, etc.
        log.info("Successfully processed USER_CREATED for userId: {}", data.getUserId());
    }

    private void handleUserUpdated(UserUpdatedEvent event) {
        UserUpdatedEvent.UserUpdateEventData data = event.getData();

        log.info("=== User Updated Event (from VirtualBank) ===");
        log.info("Event ID: {}", event.getEventId());
        log.info("Service: {}", event.getServiceName());
        log.info("User ID: {}, Username: {}, Email: {}", data.getUserId(), data.getUsername(), data.getEmail());
        log.info("Timestamp: {}", event.getTimestamp());
        log.info("=============================================");

        // TODO: Update cache, sync local data, etc.
        log.info("Successfully processed USER_UPDATED for userId: {}", data.getUserId());
    }

    private void handleUserDeleted(UserDeletedEvent event) {
        UserDeletedEvent.UserDeleteEventData data = event.getData();

        log.info("=== User Deleted Event (from VirtualBank) ===");
        log.info("Event ID: {}", event.getEventId());
        log.info("Service: {}", event.getServiceName());
        log.info("User ID: {}, Reason: {}", data.getUserId(), data.getReason());
        log.info("Timestamp: {}", event.getTimestamp());
        log.info("=============================================");

        // TODO: Remove from cache, archive transactions, etc.
        log.info("Successfully processed USER_DELETED for userId: {}", data.getUserId());
    }

    @Override
    public void handleError(BaseEvent event, Throwable throwable) {
        log.error("Error processing user event");
        log.error("Event Type: {}", event.getEventType());
        log.error("Event ID: {}", event.getEventId());
        log.error("Service Name: {}", event.getServiceName());
        log.error("Timestamp: {}", event.getTimestamp());
        log.error("Error: {}", throwable.getMessage(), throwable);
        log.warn("User event processing failed - manual intervention may be required");
    }

    @Override
    public boolean canHandle(String eventType) {
        return eventType != null && eventType.startsWith("USER_");
    }
}
