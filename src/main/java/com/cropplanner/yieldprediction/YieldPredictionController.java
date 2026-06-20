package com.cropplanner.yieldprediction;

import com.cropplanner.model.User;
import com.cropplanner.security.SessionUserResolver;
import com.cropplanner.yieldprediction.YieldDTOs.PredictYieldRequest;
import com.cropplanner.yieldprediction.YieldDTOs.RecordActualYieldRequest;
import com.cropplanner.yieldprediction.YieldDTOs.YieldRecordResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/yield-predictions")
@RequiredArgsConstructor
public class YieldPredictionController {

    private final YieldPredictionService yieldPredictionService;
    private final SessionUserResolver sessionUserResolver;

    @PostMapping
    public ResponseEntity<YieldRecordResponse> predict(
            @Valid @RequestBody PredictYieldRequest req,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        YieldRecordResponse response = yieldPredictionService.predict(user, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<YieldRecordResponse>> getAll(HttpServletRequest request) {
        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.ok(yieldPredictionService.getForUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<YieldRecordResponse> getById(@PathVariable Long id, HttpServletRequest request) {
        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.ok(yieldPredictionService.getById(user, id));
    }

    @PutMapping("/{id}/actual")
    public ResponseEntity<YieldRecordResponse> recordActual(
            @PathVariable Long id,
            @Valid @RequestBody RecordActualYieldRequest req,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        YieldRecordResponse response = yieldPredictionService.recordActualYield(user, id, req.getActualYieldKg());
        return ResponseEntity.ok(response);
    }
}
