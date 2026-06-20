package com.cropplanner.farmmap;

import com.cropplanner.exception.BusinessRuleException;
import com.cropplanner.exception.ResourceNotFoundException;
import com.cropplanner.farmmap.FarmMapDTOs.*;
import com.cropplanner.model.PlantingSchedule;
import com.cropplanner.model.User;
import com.cropplanner.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FarmMapService {

    private final FarmPlotRepository farmPlotRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public FarmPlotResponse create(User user, CreateFarmPlotRequest req) {
        Double area = AreaCalculator.calculateHectares(req.getCoordinatesJson());
        if (area == null) {
            throw new BusinessRuleException(
                    "Could not calculate area — coordinates must be a valid JSON array of [lat, lon] pairs with at least 3 points.");
        }

        PlantingSchedule schedule = resolveSchedule(user, req.getCurrentScheduleId());

        FarmPlot plot = FarmPlot.builder()
                .user(user)
                .name(req.getName().trim())
                .coordinatesJson(req.getCoordinatesJson())
                .areaHectares(area)
                .currentSchedule(schedule)
                .notes(req.getNotes())
                .build();

        FarmPlot saved = farmPlotRepository.save(plot);
        log.info("Farm plot created — user: {} name: {} area: {}ha", user.getEmail(), req.getName(), area);
        return FarmPlotResponse.from(saved);
    }

    public List<FarmPlotResponse> getForUser(User user) {
        return farmPlotRepository.findByUserOrderByNameAsc(user)
                .stream()
                .map(FarmPlotResponse::from)
                .toList();
    }

    public FarmPlotResponse getById(User user, Long plotId) {
        return FarmPlotResponse.from(getOwnedPlot(user, plotId));
    }

    @Transactional
    public FarmPlotResponse assignCrop(User user, Long plotId, Long scheduleId) {
        FarmPlot plot = getOwnedPlot(user, plotId);
        PlantingSchedule schedule = resolveSchedule(user, scheduleId);
        plot.setCurrentSchedule(schedule);
        return FarmPlotResponse.from(farmPlotRepository.save(plot));
    }

    @Transactional
    public void delete(User user, Long plotId) {
        FarmPlot plot = getOwnedPlot(user, plotId);
        farmPlotRepository.delete(plot);
    }

    private PlantingSchedule resolveSchedule(User user, Long scheduleId) {
        if (scheduleId == null) return null;
        PlantingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", scheduleId));
        if (!schedule.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Schedule", scheduleId);
        }
        return schedule;
    }

    private FarmPlot getOwnedPlot(User user, Long plotId) {
        FarmPlot plot = farmPlotRepository.findById(plotId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm plot", plotId));
        if (!plot.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Farm plot", plotId);
        }
        return plot;
    }
}
