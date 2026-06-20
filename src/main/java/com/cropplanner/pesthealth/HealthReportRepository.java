package com.cropplanner.pesthealth;

import com.cropplanner.model.PlantingSchedule;
import com.cropplanner.model.User;
import com.cropplanner.pesthealth.HealthEnums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthReportRepository extends JpaRepository<HealthReport, Long> {
    List<HealthReport> findByUserOrderByCreatedAtDesc(User user);
    List<HealthReport> findBySchedule(PlantingSchedule schedule);
    List<HealthReport> findByScheduleAndStatusNot(PlantingSchedule schedule, ReportStatus status);
}
