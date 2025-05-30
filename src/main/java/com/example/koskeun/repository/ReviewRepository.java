package com.example.koskeun.repository;

import com.example.koskeun.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserId(Long userId);

    List<Review> findByKosId(Long kosId);

    List<Review> findByKosIdOrderByCreatedAtDesc(Long kosId);

    Double findAverageRatingByKosId(Long kosId);
}
