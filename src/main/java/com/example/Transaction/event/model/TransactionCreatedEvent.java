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
public class TransactionCreatedEvent implements BaseEvent {

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
        private String status;
    }

    public static TransactionCreatedEvent from(Transaction transaction) {
        return TransactionCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("TRANSACTION_CREATED")
                .timestamp(LocalDateTime.now())
                .serviceName("transaction-service")
                .data(TransactionEventData.builder()
                        .transactionId(transaction.getTransactionId())
                        .fromAccount(transaction.getFromAccount())
                        .toAccount(transaction.getToAccount())
                        .amount(transaction.getAmount())
                        .transactionType(transaction.getType().name())
                        .status(transaction.getStatus().name())
                        .build())
                .build();
    }
}
