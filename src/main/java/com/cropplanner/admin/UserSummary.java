package com.cropplanner.admin;

import com.cropplanner.model.Role;
import com.cropplanner.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Safe projection of User — never exposes password or internal audit fields.
 * Used wherever an admin or advisor needs a user view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummary {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String location;
    private Role role;
    private String createdAt;

    public static UserSummary from(User u) {
        return UserSummary.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .location(u.getLocation())
                .role(u.getRole())
                .createdAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() : null)
                .build();
    }
}
