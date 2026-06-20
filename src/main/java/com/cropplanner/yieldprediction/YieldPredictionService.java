package com.cropplanner.yieldprediction;

import com.cropplanner.exception.BusinessRuleException;
import com.cropplanner.exception.ResourceNotFoundException;
import com.cropplanner.model.Crop;
import com.cropplanner.model.PlantingSchedule;
import com.cropplanner.model.User;
import com.cropplanner.repository.ScheduleRepository;
import com.cropplanner.yieldprediction.YieldDTOs.PredictYieldRequest;
import com.cropplanner.yieldprediction.YieldDTOs.YieldRecordResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estimates expected harvest quantity from a crop's average yield-per-hectare
 * and the farmer's planted area, and lets farmers later record what they
 * actually harvested for predicted-vs-actual comparison.
 *
 * Deliberately a simple, explainable multiplication (area x avg yield) rather
 * than a black-box model — small-scale farmers need to understand and trust
 * the number, and we don't have enough historical data yet to justify
 * anything more sophisticated. This is the natural place to plug in
 * weather-adjusted predictions later (e.g. reduce the estimate if the
 * Weather Integration module flags drought risk for the growing period).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class YieldPredictionService {

    private final YieldRecordRepository yieldRecordRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public YieldRecordResponse predict(User user, PredictYieldRequest req) {
        PlantingSchedule schedule = getOwnedSchedule(user, req.getScheduleId());

        if (yieldRecordRepository.findBySchedule(schedule).isPresent()) {
            throw new BusinessRuleException(
                    "A yield prediction already exists for this schedule. Update the farm area on the existing prediction instead.");
        }

        Crop crop = schedule.getCrop();
        if (crop.getAvgYieldKgPerHectare() == null) {
            throw new BusinessRuleException(
                    "No average yield data is available for " + crop.getName() + " yet, so a prediction cannot be calculated.");
        }

        double predictedYieldKg = req.getFarmAreaHectares() * crop.getAvgYieldKgPerHectare();

        YieldRecord record = YieldRecord.builder()
                .schedule(schedule)
                .farmAreaHectares(req.getFarmAreaHectares())
                .predictedYieldKg(predictedYieldKg)
                .build();

        YieldRecord saved = yieldRecordRepository.save(record);
        log.info("Yield predicted — user: {} crop: {} predicted: {}kg", user.getEmail(), crop.getName(), predictedYieldKg);
        return YieldRecordResponse.from(saved);
    }

    @Transactional
    public YieldRecordResponse recordActualYield(User user, Long yieldRecordId, double actualYieldKg) {
        YieldRecord record = getOwnedYieldRecord(user, yieldRecordId);
        record.setActualYieldKg(actualYieldKg);
        record.setActualRecordedAt(LocalDateTime.now());
        YieldRecord saved = yieldRecordRepository.save(record);
        return YieldRecordResponse.from(saved);
    }

    public List<YieldRecordResponse> getForUser(User user) {
        return yieldRecordRepository.findBySchedule_User(user)
                .stream()
                .map(YieldRecordResponse::from)
                .toList();
    }

    public YieldRecordResponse getById(User user, Long yieldRecordId) {
        return YieldRecordResponse.from(getOwnedYieldRecord(user, yieldRecordId));
    }

    private PlantingSchedule getOwnedSchedule(User user, Long scheduleId) {
        PlantingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", scheduleId));
        if (!schedule.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Schedule", scheduleId);
        }
        return schedule;
    }

    private YieldRecord getOwnedYieldRecord(User user, Long yieldRecordId) {
        YieldRecord record = yieldRecordRepository.findById(yieldRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Yield prediction", yieldRecordId));
        if (!record.getSchedule().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Yield prediction", yieldRecordId);
        }
        return record;
    }
}
