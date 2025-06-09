package com.example.koskeun.repository;

import com.example.koskeun.model.Kos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KosRepository extends JpaRepository<Kos, Long> {
    // Basic queries
    List<Kos> findByOwnerId(Long ownerId);

    List<Kos> findByStatus(String status);

    List<Kos> findByType(String type);

    List<Kos> findByCategory(String category);

    // Paginated queries
    Page<Kos> findAllByOrderByIdDesc(Pageable pageable);

    Page<Kos> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Kos> findByStatusAndType(String status, String type, Pageable pageable);

    Page<Kos> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(String name, String address,
            Pageable pageable);

    // Search queries
    List<Kos> findTop5ByStatusAndTypeOrderByIdDesc(String status, String type);

    List<Kos> findByStatusAndTypeAndIdNot(String status, String type, Long id);
}
