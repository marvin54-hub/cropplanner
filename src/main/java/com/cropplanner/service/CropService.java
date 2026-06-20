package com.cropplanner.service;

import com.cropplanner.exception.ResourceNotFoundException;
import com.cropplanner.model.Crop;
import com.cropplanner.repository.CropRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Owns crop-catalog reads. Kept deliberately small for now — this is the
 * natural extension point for Crop Yield Prediction (Phase 2), which will
 * need crop-specific yield-per-hectare data alongside growthDurationDays.
 */
@Service
@RequiredArgsConstructor
public class CropService {

    private final CropRepository cropRepository;

    @Cacheable("crops")
    public List<Crop> getAll() {
        return cropRepository.findAllByOrderByNameAsc();
    }

    @Cacheable(value = "cropsBySeason", key = "#season")
    public List<Crop> getBySeason(String season) {
        return cropRepository.findBySeason(season);
    }

    public Crop getById(Long id) {
        return cropRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crop", id));
    }

    /** Paged crop listing, mainly intended for admin/larger-catalog use as the crop list grows. */
    public Page<Crop> getAllPaged(Pageable pageable) {
        return cropRepository.findAll(pageable);
    }
}
