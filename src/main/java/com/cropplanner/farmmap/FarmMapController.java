package com.cropplanner.farmmap;

import com.cropplanner.farmmap.FarmMapDTOs.*;
import com.cropplanner.model.DTOs.ApiResult;
import com.cropplanner.model.User;
import com.cropplanner.security.SessionUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farm-plots")
@RequiredArgsConstructor
public class FarmMapController {

    private final FarmMapService farmMapService;
    private final SessionUserResolver sessionUserResolver;

    @PostMapping
    public ResponseEntity<FarmPlotResponse> create(
            @Valid @RequestBody CreateFarmPlotRequest req,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(farmMapService.create(user, req));
    }

    @GetMapping
    public ResponseEntity<List<FarmPlotResponse>> getAll(HttpServletRequest request) {
        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.ok(farmMapService.getForUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FarmPlotResponse> getById(@PathVariable Long id, HttpServletRequest request) {
        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.ok(farmMapService.getById(user, id));
    }

    @PutMapping("/{id}/crop")
    public ResponseEntity<FarmPlotResponse> assignCrop(
            @PathVariable Long id,
            @RequestBody AssignCropRequest req,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.ok(farmMapService.assignCrop(user, id, req.getScheduleId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult> delete(@PathVariable Long id, HttpServletRequest request) {
        User user = sessionUserResolver.requireCurrentUser(request);
        farmMapService.delete(user, id);
        return ResponseEntity.ok(ApiResult.ok("Farm plot deleted."));
    }
}
