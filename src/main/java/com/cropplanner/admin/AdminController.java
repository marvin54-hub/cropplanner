package com.cropplanner.admin;

import com.cropplanner.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only endpoints. Access is controlled at the URL level in
 * SecurityConfig (.requestMatchers("/api/admin/**").hasRole("ADMIN")),
 * so no per-method @PreAuthorize is needed — the filter chain rejects
 * non-admins before they reach this controller.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserSummary>> getAllUsers(
            @RequestParam(required = false) Role role) {

        if (role != null) {
            return ResponseEntity.ok(adminService.getUsersByRole(role));
        }
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserSummary> assignRole(
            @PathVariable Long id,
            @RequestParam Role role) {

        return ResponseEntity.ok(adminService.assignRole(id, role));
    }
}
