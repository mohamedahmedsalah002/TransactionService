package com.example.Transaction.event.model;

import java.time.LocalDateTime;
import java.util.UUID;

public interface BaseEvent {
    UUID getEventId();
    String getEventType();
    LocalDateTime getTimestamp();
    String getServiceName();
}
