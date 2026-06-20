package com.cropplanner.service;

import com.cropplanner.model.User;
import com.cropplanner.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

/**
 * Owns the login/logout session lifecycle. This was previously embedded
 * directly in AuthController; moving it here lets the controller focus on
 * translating HTTP <-> DTOs while this class owns the Spring Security
 * interaction (authenticate, rotate session, stash userId).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    /**
     * Authenticates the given credentials and establishes a fresh session
     * for the user. Throws BadCredentialsException on failure (handled by
     * GlobalExceptionHandler).
     */
    public User login(String email, String password, HttpServletRequest httpRequest) {
        String normalizedEmail = email.toLowerCase().trim();

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, password)
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Invalidate any pre-existing session first (session fixation prevention)
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = httpRequest.getSession(true);
        newSession.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );

        User user = userRepository.findByEmail(normalizedEmail).orElseThrow();
        newSession.setAttribute("userId", user.getId());
        newSession.setAttribute("userFullName", user.getFullName());
        newSession.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

        log.info("User logged in: {}", user.getEmail());
        return user;
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }
}
