package com.example.Transaction.event.model;

import com.example.Transaction.model.entity.Transaction;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCompletedEvent implements BaseEvent {

    private UUID eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String serviceName;
    private TransactionEventData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionEventData {
        private Long transactionId;
        private Long fromAccount;
        private Long toAccount;
        private BigDecimal amount;
        private String transactionType;
        private LocalDateTime completedAt;
    }

    public static TransactionCompletedEvent from(Transaction transaction) {
        return TransactionCompletedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("TRANSACTION_COMPLETED")
                .timestamp(LocalDateTime.now())
                .serviceName("transaction-service")
                .data(TransactionEventData.builder()
                        .transactionId(transaction.getTransactionId())
                        .fromAccount(transaction.getFromAccount())
                        .toAccount(transaction.getToAccount())
                        .amount(transaction.getAmount())
                        .transactionType(transaction.getType().name())
                        .completedAt(transaction.getTimestamp())
                        .build())
                .build();
    }
}
