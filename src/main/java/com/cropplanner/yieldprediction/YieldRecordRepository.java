package com.cropplanner.yieldprediction;

import com.cropplanner.model.PlantingSchedule;
import com.cropplanner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface YieldRecordRepository extends JpaRepository<YieldRecord, Long> {
    Optional<YieldRecord> findBySchedule(PlantingSchedule schedule);
    List<YieldRecord> findBySchedule_User(User user);
}
