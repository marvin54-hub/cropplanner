package com.cropplanner.controller;

import com.cropplanner.model.DTOs.*;
import com.cropplanner.model.User;
import com.cropplanner.security.jwt.JwtTokenProvider;
import com.cropplanner.service.AuthService;
import com.cropplanner.service.UserService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin HTTP layer: parses requests, delegates to AuthService/UserService,
 * maps results to response DTOs. Validation errors and business-rule
 * violations are handled by GlobalExceptionHandler — no try/catch needed.
 *
 * Exposes two auth paths:
 *  - /login   → session-based (used by the HTML frontend)
 *  - /token   → JWT-based (used by mobile/API clients)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.success("Registration successful! Please login.", user));
    }

    /** Session-based login — for the HTML frontend. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest httpRequest) {

        User user = authService.login(req.getEmail(), req.getPassword(), httpRequest);
        return ResponseEntity.ok(AuthResponse.success("Login successful!", user));
    }

    /**
     * JWT token endpoint — for mobile / API clients.
     * Returns a Bearer token that can be sent in the Authorization header
     * for subsequent requests instead of using a session cookie.
     */
    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> getToken(@Valid @RequestBody LoginRequest req,
                                                         HttpServletRequest httpRequest) {
        // Reuse the same authentication logic; we just also issue a token
        User user = authService.login(req.getEmail(), req.getPassword(), httpRequest);
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        Map<String, String> body = new HashMap<>();
        body.put("token", token);
        body.put("email", user.getEmail());
        body.put("role", user.getRole().name());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/logout")
    public ResponseEntity<ApiResult> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResult.ok("Logged out successfully."));
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> session(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> res = new HashMap<>();
        if (session != null && session.getAttribute("userId") != null) {
            res.put("loggedIn", true);
            res.put("userId", session.getAttribute("userId"));
            res.put("fullName", session.getAttribute("userFullName"));
        } else {
            res.put("loggedIn", false);
        }
        return ResponseEntity.ok(res);
    }
}
