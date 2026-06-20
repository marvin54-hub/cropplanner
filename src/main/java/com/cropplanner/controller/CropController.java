package com.cropplanner.controller;

import com.cropplanner.model.Crop;
import com.cropplanner.service.CropService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crops")
@RequiredArgsConstructor
public class CropController {

    private final CropService cropService;

    @GetMapping
    public ResponseEntity<List<Crop>> getAllCrops() {
        return ResponseEntity.ok(cropService.getAll());
    }

    /** Paged variant, e.g. /api/crops/paged?page=0&size=10&sort=name,asc */
    @GetMapping("/paged")
    public ResponseEntity<Page<Crop>> getAllCropsPaged(Pageable pageable) {
        return ResponseEntity.ok(cropService.getAllPaged(pageable));
    }

    @GetMapping("/season/{season}")
    public ResponseEntity<List<Crop>> getBySeason(@PathVariable String season) {
        return ResponseEntity.ok(cropService.getBySeason(season));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Crop> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cropService.getById(id));
    }
}
