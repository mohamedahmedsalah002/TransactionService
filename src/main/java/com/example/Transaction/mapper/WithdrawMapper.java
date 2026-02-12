package com.example.Transaction.mapper;

import com.example.Transaction.model.dto.request.WithdrawRequest;
import com.example.Transaction.model.entity.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class WithdrawMapper {
    
    /**
     * Maps a WithdrawRequest to a Transaction entity
     * @param request the withdraw request
     * @return Transaction entity for withdraw operation
     */
    public Transaction toEntity(WithdrawRequest request) {
        if (request == null) {
            return null;
        }
        
        return Transaction.builder()
                .fromAccount(request.getAccountId())
                .toAccount(0L) // System account for withdrawals
                .amount(request.getAmount())
                .type(Transaction.TransactionType.WITHDRAW)
                .timestamp(LocalDateTime.now())
                .status(Transaction.TransactionStatus.PENDING)
                .build();
    }
}
