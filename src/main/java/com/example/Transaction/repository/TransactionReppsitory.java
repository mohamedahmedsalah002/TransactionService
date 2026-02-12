package com.example.Transaction.repository;

import com.example.Transaction.model.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionReppsitory extends JpaRepository<Transaction, Long> {
    
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount = :accountId OR t.toAccount = :accountId ORDER BY t.timestamp DESC")
    List<Transaction> findByAccountId(@Param("accountId") Long accountId);
    
    List<Transaction> findByFromAccountOrderByTimestampDesc(Long fromAccount);
    
    List<Transaction> findByToAccountOrderByTimestampDesc(Long toAccount);
    
    List<Transaction> findByStatusOrderByTimestampDesc(Transaction.TransactionStatus status);
}
