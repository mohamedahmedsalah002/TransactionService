package com.example.Transaction.mapper;

import com.example.Transaction.model.dto.request.TransferRequest;
import com.example.Transaction.model.entity.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TransferMapper {
    
    /**
     * Maps a TransferRequest to a Transaction entity
     * @param request the transfer request
     * @return Transaction entity for transfer operation
     */
    public Transaction toEntity(TransferRequest request) {
        if (request == null) {
            return null;
        }
        
        return Transaction.builder()
                .fromAccount(request.getFromAccountId())
                .toAccount(request.getToAccountId())
                .amount(request.getAmount())
                .type(Transaction.TransactionType.TRANSFER)
                .timestamp(LocalDateTime.now())
                .status(Transaction.TransactionStatus.PENDING)
                .build();
    }
}
