package com.example.Transaction.event.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFailedEvent implements BaseEvent {

    private UUID eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String serviceName;
    private TransactionFailureData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionFailureData {
        private Long transactionId;
        private Long fromAccount;
        private Long toAccount;
        private BigDecimal amount;
        private String transactionType;
        private String failureReason;
        private LocalDateTime failedAt;
    }

    public static TransactionFailedEvent create(Long transactionId, Long fromAccount, Long toAccount, 
                                                 BigDecimal amount, String transactionType, String failureReason) {
        return TransactionFailedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("TRANSACTION_FAILED")
                .timestamp(LocalDateTime.now())
                .serviceName("transaction-service")
                .data(TransactionFailureData.builder()
                        .transactionId(transactionId)
                        .fromAccount(fromAccount)
                        .toAccount(toAccount)
                        .amount(amount)
                        .transactionType(transactionType)
                        .failureReason(failureReason)
                        .failedAt(LocalDateTime.now())
                        .build())
                .build();
    }
}
