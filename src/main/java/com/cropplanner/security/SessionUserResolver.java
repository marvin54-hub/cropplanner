package com.cropplanner.security;

import com.cropplanner.exception.UnauthorizedAccessException;
import com.cropplanner.model.User;
import com.cropplanner.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the currently logged-in User from the HTTP session.
 *
 * Centralising this here means every service can ask "who is making this
 * call?" the same way, instead of each controller re-implementing session
 * lookups (which is what the original ScheduleController / AuthController did).
 */
@Component
@RequiredArgsConstructor
public class SessionUserResolver {

    private final UserRepository userRepository;

    /**
     * Returns the logged-in user, or null if no one is logged in.
     * Use this when "not logged in" is a valid, non-exceptional outcome.
     */
    public User getCurrentUserOrNull(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return null;
        }
        Long id = (Long) session.getAttribute("userId");
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Returns the logged-in user, or throws UnauthorizedAccessException.
     * Use this in services where the caller must already be authenticated.
     */
    public User requireCurrentUser(HttpServletRequest request) {
        User user = getCurrentUserOrNull(request);
        if (user == null) {
            throw new UnauthorizedAccessException("Please login first.");
        }
        return user;
    }
}
