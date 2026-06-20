package com.cropplanner.service;

import com.cropplanner.exception.BusinessRuleException;
import com.cropplanner.exception.ResourceNotFoundException;
import com.cropplanner.model.DTOs.RegisterRequest;
import com.cropplanner.model.User;
import com.cropplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns all User-related business rules: registration, normalisation of
 * email/casing, and lookups. AuthController used to do this inline —
 * pulling it out here means the same rules can be reused (e.g. by an
 * admin "create user" endpoint later) without duplicating logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest req) {
        String normalizedEmail = req.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessRuleException("This email is already registered. Please login.");
        }

        User user = User.builder()
                .fullName(req.getFullName().trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .location(req.getLocation())
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getEmail());
        return saved;
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
