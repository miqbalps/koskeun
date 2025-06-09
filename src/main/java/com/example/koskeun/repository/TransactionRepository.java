package com.example.koskeun.repository;

import com.example.koskeun.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByOwnerId(Long ownerId, Sort sort);

    List<Transaction> findByKosId(Long kosId);

    List<Transaction> findByStatus(String status);

    List<Transaction> findByUserIdAndStatus(Long userId, String status);

    List<Transaction> findByKosIdAndStatus(Long kosId, String status);
}
