package com.example.koskeun.repository;

import com.example.koskeun.model.KosApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface KosApprovalRepository extends JpaRepository<KosApproval, Long> {
    List<KosApproval> findByKosId(Long kosId);

    List<KosApproval> findByAdminId(Long adminId);

    List<KosApproval> findByStatus(String status);

    Optional<KosApproval> findTopByKosIdOrderByReviewedAtDesc(Long kosId);
}
