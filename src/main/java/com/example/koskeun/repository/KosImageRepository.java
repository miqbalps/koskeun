package com.example.koskeun.repository;

import com.example.koskeun.model.KosImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KosImageRepository extends JpaRepository<KosImage, Long> {
    List<KosImage> findByKosId(Long kosId);

    List<KosImage> findByKosIdAndType(Long kosId, String type);
}
