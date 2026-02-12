package com.example.Transaction.mapper;

import com.example.Transaction.model.dto.response.TransactionDto;
import com.example.Transaction.model.entity.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {
    
    public TransactionDto toDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        return TransactionDto.builder()
                .transactionId(transaction.getTransactionId())
                .fromAccount(transaction.getFromAccount())
                .toAccount(transaction.getToAccount())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .timestamp(transaction.getTimestamp())
                .status(transaction.getStatus())
                .build();
    }
    
    public Transaction toEntity(TransactionDto dto) {
        if (dto == null) {
            return null;
        }
        
        return Transaction.builder()
                .transactionId(dto.getTransactionId())
                .fromAccount(dto.getFromAccount())
                .toAccount(dto.getToAccount())
                .amount(dto.getAmount())
                .type(dto.getType())
                .timestamp(dto.getTimestamp())
                .status(dto.getStatus())
                .build();
    }
    
    public List<TransactionDto> toDtoList(List<Transaction> transactions) {
        if (transactions == null) {
            return null;
        }
        
        return transactions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
