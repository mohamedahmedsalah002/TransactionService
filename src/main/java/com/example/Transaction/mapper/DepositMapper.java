package com.example.Transaction.mapper;

import com.example.Transaction.model.dto.request.DepositRequest;
import com.example.Transaction.model.entity.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DepositMapper {
    
    /**
     * Maps a DepositRequest to a Transaction entity
     * @param request the deposit request
     * @return Transaction entity for deposit operation
     */
    public Transaction toEntity(DepositRequest request) {
        if (request == null) {
            return null;
        }
        
        return Transaction.builder()
                .fromAccount(0L) // System account for deposits
                .toAccount(request.getAccountId())
                .amount(request.getAmount())
                .type(Transaction.TransactionType.DEPOSIT)
                .timestamp(LocalDateTime.now())
                .status(Transaction.TransactionStatus.PENDING)
                .build();
    }
}
