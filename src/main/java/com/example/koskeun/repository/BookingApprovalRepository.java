package com.example.koskeun.repository;

import com.example.koskeun.model.BookingApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingApprovalRepository extends JpaRepository<BookingApproval, Long> {
    List<BookingApproval> findByTransactionId(Long transactionId);

    List<BookingApproval> findByOwnerId(Long ownerId);

    List<BookingApproval> findByStatus(String status);

    Optional<BookingApproval> findTopByTransactionIdOrderByReviewedAtDesc(Long transactionId);
}
