package com.cropplanner.inventory;

import com.cropplanner.inventory.InventoryDTOs.*;
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
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final SessionUserResolver sessionUserResolver;

    @PostMapping
    public ResponseEntity<InventoryItemResponse> create(
            @Valid @RequestBody CreateInventoryItemRequest req,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.create(user, req));
    }

    @GetMapping
    public ResponseEntity<List<InventoryItemResponse>> getAll(
            @RequestParam(required = false) InventoryItemType type,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        if (type != null) {
            return ResponseEntity.ok(inventoryService.getForUserByType(user, type));
        }
        return ResponseEntity.ok(inventoryService.getForUser(user));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItemResponse>> getLowStock(HttpServletRequest request) {
        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.ok(inventoryService.getLowStock(user));
    }

    @PutMapping("/{id}/adjust")
    public ResponseEntity<InventoryItemResponse> adjustQuantity(
            @PathVariable Long id,
            @Valid @RequestBody AdjustQuantityRequest req,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.ok(inventoryService.adjustQuantity(user, id, req.getDelta()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult> delete(@PathVariable Long id, HttpServletRequest request) {
        User user = sessionUserResolver.requireCurrentUser(request);
        inventoryService.delete(user, id);
        return ResponseEntity.ok(ApiResult.ok("Inventory item deleted."));
    }
}
