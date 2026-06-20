package com.cropplanner.yieldprediction;

import com.cropplanner.exception.BusinessRuleException;
import com.cropplanner.model.*;
import com.cropplanner.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YieldPredictionServiceTest {

    @Mock private YieldRecordRepository yieldRecordRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @InjectMocks private YieldPredictionService yieldPredictionService;

    private User user;
    private Crop crop;
    private PlantingSchedule schedule;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("nomvula@farm.za").role(Role.FARMER).build();
        crop = Crop.builder().id(2L).name("Tomatoes").season("Summer")
                .growthDurationDays(75).avgYieldKgPerHectare(20000.0).build();
        schedule = PlantingSchedule.builder().id(3L).user(user).crop(crop)
                .plantingDate(LocalDate.now()).expectedHarvestDate(LocalDate.now().plusDays(75))
                .status("Planted").build();
    }

    @Test
    @DisplayName("predict() computes predictedYieldKg = farmArea × avgYieldPerHectare")
    void predict_calculatesYieldCorrectly() {
        YieldDTOs.PredictYieldRequest req = new YieldDTOs.PredictYieldRequest();
        req.setScheduleId(3L);
        req.setFarmAreaHectares(0.5);

        when(scheduleRepository.findById(3L)).thenReturn(Optional.of(schedule));
        when(yieldRecordRepository.findBySchedule(schedule)).thenReturn(Optional.empty());
        when(yieldRecordRepository.save(any())).thenAnswer(inv -> {
            YieldRecord r = inv.getArgument(0);
            r = YieldRecord.builder()
                    .id(10L).schedule(schedule)
                    .farmAreaHectares(r.getFarmAreaHectares())
                    .predictedYieldKg(r.getPredictedYieldKg())
                    .build();
            return r;
        });

        YieldDTOs.YieldRecordResponse response = yieldPredictionService.predict(user, req);

        assertThat(response.getPredictedYieldKg()).isEqualTo(10000.0); // 0.5 ha × 20000 kg/ha
        assertThat(response.getActualYieldKg()).isNull();
    }

    @Test
    @DisplayName("predict() throws if a prediction already exists for this schedule")
    void predict_throwsIfAlreadyExists() {
        YieldDTOs.PredictYieldRequest req = new YieldDTOs.PredictYieldRequest();
        req.setScheduleId(3L);
        req.setFarmAreaHectares(1.0);

        YieldRecord existing = YieldRecord.builder().id(5L).schedule(schedule)
                .farmAreaHectares(1.0).predictedYieldKg(20000.0).build();

        when(scheduleRepository.findById(3L)).thenReturn(Optional.of(schedule));
        when(yieldRecordRepository.findBySchedule(schedule)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> yieldPredictionService.predict(user, req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("variance% is computed correctly when actual yield recorded")
    void variancePct_computedCorrectly() {
        YieldRecord record = YieldRecord.builder()
                .id(10L).schedule(schedule)
                .farmAreaHectares(1.0)
                .predictedYieldKg(20000.0)
                .actualYieldKg(18000.0) // 10% under prediction
                .build();

        YieldDTOs.YieldRecordResponse response = YieldDTOs.YieldRecordResponse.from(record);

        assertThat(response.getVariancePct()).isCloseTo(-10.0, within(0.01));
    }
}
