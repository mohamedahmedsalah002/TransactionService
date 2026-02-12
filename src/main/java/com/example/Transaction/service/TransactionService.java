package com.example.Transaction.service;

import com.example.Transaction.event.model.TransactionCompletedEvent;
import com.example.Transaction.event.model.TransactionCreatedEvent;
import com.example.Transaction.event.model.TransactionFailedEvent;
import com.example.Transaction.event.producer.TransactionEventProducer;
import com.example.Transaction.mapper.DepositMapper;
import com.example.Transaction.mapper.TransactionMapper;
import com.example.Transaction.mapper.TransferMapper;
import com.example.Transaction.mapper.WithdrawMapper;
import com.example.Transaction.model.dto.request.DepositRequest;
import com.example.Transaction.model.dto.request.TransferRequest;
import com.example.Transaction.model.dto.request.WithdrawRequest;
import com.example.Transaction.model.dto.response.BalanceResponse;
import com.example.Transaction.model.dto.response.TransactionDto;
import com.example.Transaction.model.entity.Transaction;
import com.example.Transaction.repository.TransactionReppsitory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    
    private final TransactionReppsitory transactionRepository;
    private final TransactionMapper transactionMapper;
    private final DepositMapper depositMapper;
    private final WithdrawMapper withdrawMapper;
    private final TransferMapper transferMapper;
    private final TransactionEventProducer eventProducer;
    
    @Transactional
    public TransactionDto deposit(Long accountId, BigDecimal amount) {
        log.info("Processing deposit for account: {}, amount: {}", accountId, amount);
        
        validateAmount(amount);
        
        // Create deposit request and map to transaction entity
        DepositRequest request = new DepositRequest(accountId, amount);
        Transaction transaction = depositMapper.toEntity(request);
        
        try {
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            // Publish Kafka events
            publishTransactionEvents(savedTransaction, null);
            
            log.info("Deposit completed successfully: {}", savedTransaction.getTransactionId());
            return transactionMapper.toDto(savedTransaction);
        } catch (Exception e) {
            log.error("Deposit failed for account: {}", accountId, e);
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            Transaction failedTransaction = transactionRepository.save(transaction);
            
            // Publish failure event
            publishTransactionEvents(failedTransaction, e.getMessage());
            
            throw new RuntimeException("Deposit transaction failed: " + e.getMessage());
        }
    }
    
    @Transactional
    public TransactionDto withdraw(Long accountId, BigDecimal amount) {
        log.info("Processing withdrawal for account: {}, amount: {}", accountId, amount);
        
        validateAmount(amount);
        
        // Create withdraw request and map to transaction entity
        WithdrawRequest request = new WithdrawRequest(accountId, amount);
        Transaction transaction = withdrawMapper.toEntity(request);
        
        try {
            // Here you would typically check account balance
            // For now, we'll proceed with the transaction
            
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            // Publish Kafka events
            publishTransactionEvents(savedTransaction, null);
            
            log.info("Withdrawal completed successfully: {}", savedTransaction.getTransactionId());
            return transactionMapper.toDto(savedTransaction);
        } catch (Exception e) {
            log.error("Withdrawal failed for account: {}", accountId, e);
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            Transaction failedTransaction = transactionRepository.save(transaction);
            
            // Publish failure event
            publishTransactionEvents(failedTransaction, e.getMessage());
            
            throw new RuntimeException("Withdrawal transaction failed: " + e.getMessage());
        }
    }
    
    @Transactional
    public TransactionDto transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        log.info("Processing transfer from account: {} to account: {}, amount: {}", 
                fromAccountId, toAccountId, amount);
        
        validateAmount(amount);
        validateTransfer(fromAccountId, toAccountId);
        
        // Create transfer request and map to transaction entity
        TransferRequest request = new TransferRequest(fromAccountId, toAccountId, amount);
        Transaction transaction = transferMapper.toEntity(request);
        
        try {
            // Here you would typically:
            // 1. Check if fromAccount has sufficient balance
            // 2. Verify both accounts exist
            // 3. Update account balances
            
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            // Publish Kafka events
            publishTransactionEvents(savedTransaction, null);
            
            log.info("Transfer completed successfully: {}", savedTransaction.getTransactionId());
            return transactionMapper.toDto(savedTransaction);
        } catch (Exception e) {
            log.error("Transfer failed from account: {} to account: {}", fromAccountId, toAccountId, e);
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            Transaction failedTransaction = transactionRepository.save(transaction);
            
            // Publish failure event
            publishTransactionEvents(failedTransaction, e.getMessage());
            
            throw new RuntimeException("Transfer transaction failed: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(Long id) {
        log.info("Fetching transaction with id: {}", id);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        
        return transactionMapper.toDto(transaction);
    }
    
    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionHistory(Long accountId) {
        log.info("Fetching transaction history for account: {}", accountId);
        
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);
        return transactionMapper.toDtoList(transactions);
    }
    
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Long accountId) {
        log.info("Calculating balance for account: {}", accountId);
        
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);
        
        BigDecimal totalDeposits = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;
        BigDecimal totalTransfersIn = BigDecimal.ZERO;
        BigDecimal totalTransfersOut = BigDecimal.ZERO;
        
        for (Transaction tx : transactions) {
            // Only count COMPLETED transactions
            if (tx.getStatus() != Transaction.TransactionStatus.COMPLETED) {
                continue;
            }
            
            switch (tx.getType()) {
                case DEPOSIT:
                    // Deposit: toAccount = accountId, money comes IN
                    if (tx.getToAccount().equals(accountId)) {
                        totalDeposits = totalDeposits.add(tx.getAmount());
                    }
                    break;
                    
                case WITHDRAW:
                    // Withdraw: fromAccount = accountId, money goes OUT
                    if (tx.getFromAccount().equals(accountId)) {
                        totalWithdrawals = totalWithdrawals.add(tx.getAmount());
                    }
                    break;
                    
                case TRANSFER:
                    // Transfer IN: toAccount = accountId
                    if (tx.getToAccount().equals(accountId)) {
                        totalTransfersIn = totalTransfersIn.add(tx.getAmount());
                    }
                    // Transfer OUT: fromAccount = accountId
                    if (tx.getFromAccount().equals(accountId)) {
                        totalTransfersOut = totalTransfersOut.add(tx.getAmount());
                    }
                    break;
            }
        }
        
        // Balance = deposits + transfers_in - withdrawals - transfers_out
        BigDecimal balance = totalDeposits
                .add(totalTransfersIn)
                .subtract(totalWithdrawals)
                .subtract(totalTransfersOut);
        
        log.info("Balance for account {}: {}", accountId, balance);
        
        return BalanceResponse.builder()
                .accountId(accountId)
                .balance(balance)
                .totalDeposits(totalDeposits)
                .totalWithdrawals(totalWithdrawals)
                .totalTransfersIn(totalTransfersIn)
                .totalTransfersOut(totalTransfersOut)
                .totalTransactions(transactions.size())
                .calculatedAt(LocalDateTime.now())
                .build();
    }
    
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
    
    private void validateTransfer(Long fromAccountId, Long toAccountId) {
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
    }
    
    /**
     * Publish transaction events to Kafka
     */
    private void publishTransactionEvents(Transaction transaction, String failureReason) {
        try {
            // Always publish created event first
            TransactionCreatedEvent createdEvent = TransactionCreatedEvent.from(transaction);
            eventProducer.publishTransactionCreated(createdEvent);
            
            // Publish completed or failed event based on status
            if (transaction.getStatus() == Transaction.TransactionStatus.COMPLETED) {
                TransactionCompletedEvent completedEvent = TransactionCompletedEvent.from(transaction);
                eventProducer.publishTransactionCompleted(completedEvent);
            } else if (transaction.getStatus() == Transaction.TransactionStatus.FAILED) {
                TransactionFailedEvent failedEvent = TransactionFailedEvent.create(
                        transaction.getTransactionId(),
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        transaction.getAmount(),
                        transaction.getType().name(),
                        failureReason != null ? failureReason : "Unknown error"
                );
                eventProducer.publishTransactionFailed(failedEvent);
            }
        } catch (Exception e) {
            log.error("Failed to publish transaction events for transactionId: {}", 
                    transaction.getTransactionId(), e);
            // Don't fail the transaction if Kafka event publishing fails
        }
    }
}
