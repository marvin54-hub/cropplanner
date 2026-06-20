package com.cropplanner.repository;

import com.cropplanner.model.Crop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CropRepository extends JpaRepository<Crop, Long> {
    List<Crop> findBySeason(String season);
    List<Crop> findAllByOrderByNameAsc();
    Page<Crop> findBySeason(String season, Pageable pageable);
}
