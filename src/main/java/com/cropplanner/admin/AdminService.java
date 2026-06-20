package com.cropplanner.admin;

import com.cropplanner.exception.BusinessRuleException;
import com.cropplanner.exception.ResourceNotFoundException;
import com.cropplanner.model.Role;
import com.cropplanner.model.User;
import com.cropplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;

    public List<UserSummary> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserSummary::from)
                .toList();
    }

    public List<UserSummary> getUsersByRole(Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(UserSummary::from)
                .toList();
    }

    @Transactional
    public UserSummary assignRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getRole() == newRole) {
            throw new BusinessRuleException("User already has the role: " + newRole);
        }

        Role previous = user.getRole();
        user.setRole(newRole);
        User saved = userRepository.save(user);
        log.info("Role changed — user: {} {} -> {}", saved.getEmail(), previous, newRole);
        return UserSummary.from(saved);
    }
}
