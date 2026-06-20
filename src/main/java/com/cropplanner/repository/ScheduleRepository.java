package com.cropplanner.repository;

import com.cropplanner.model.PlantingSchedule;
import com.cropplanner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<PlantingSchedule, Long> {

    List<PlantingSchedule> findByUserAndDeletedAtIsNullOrderByPlantingDateDesc(User user);

    List<PlantingSchedule> findByUserAndStatusNotAndExpectedHarvestDateGreaterThanEqualAndDeletedAtIsNullOrderByExpectedHarvestDateAsc(
            User user, String status, LocalDate date);

    long countByUserAndDeletedAtIsNull(User user);

    /** Used for ownership checks — intentionally not filtering deleted, to still resolve the ID. */
    Optional<PlantingSchedule> findByIdAndUser(Long id, User user);
}
