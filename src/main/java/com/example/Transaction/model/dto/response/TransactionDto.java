package com.example.Transaction.model.dto.response;

import com.example.Transaction.model.entity.Transaction.TransactionStatus;
import com.example.Transaction.model.entity.Transaction.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
    
    private Long transactionId;
    
    @NotNull(message = "From account is required")
    private Long fromAccount;
    
    @NotNull(message = "To account is required")
    private Long toAccount;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    @JsonFormat(pattern = "yyyy-MM-dd'Dockerfile'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private TransactionStatus status;
}
