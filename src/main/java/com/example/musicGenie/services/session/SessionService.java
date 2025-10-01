package com.example.musicGenie.services.session;

import java.time.Instant;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.token.TokenService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {
    private static final String USER_ID_KEY = "USER_ID";
    private static final String ACCESS_TOKEN_KEY = "ACCESS_TOKEN";
    private static final String ACCESS_TOKEN_EXPIRY_KEY = "ACCESS_TOKEN_EXPIRY";
    public void createUserSession(HttpSession session, Long userId,
                                  String accessToken,
                                  Instant expiry) {
        session.setAttribute(USER_ID_KEY, userId);
        session.setAttribute(ACCESS_TOKEN_KEY, accessToken);
        session.setAttribute(ACCESS_TOKEN_EXPIRY_KEY, expiry);
        log.debug("Created session for user: {}", userId);
    }

    public void updateAccessToken(HttpSession session, String accessToken, Instant expiry) {
        session.setAttribute(ACCESS_TOKEN_KEY, accessToken);
        session.setAttribute(ACCESS_TOKEN_EXPIRY_KEY, expiry);
        // Set issued at to current time when updating
        log.debug("Updated access token in session with new expiry: {}", expiry);
    }

    public Long getUserId(HttpSession session) {
        return (Long) session.getAttribute(USER_ID_KEY);
    }

    public boolean isAuthenticated(HttpSession session) {
        return getUserId(session) != null;
    }

    public String getAccessToken(HttpSession session) {
        return (String) session.getAttribute(ACCESS_TOKEN_KEY);
    }

    public Instant getAccessTokenExpiry(HttpSession session) {
        return (Instant) session.getAttribute(ACCESS_TOKEN_EXPIRY_KEY);
    }


    public boolean isAccessTokenExpired(HttpSession session) {
        Instant expiry = getAccessTokenExpiry(session);
        return expiry != null && Instant.now().isAfter(expiry);
    }

    public void invalidateSession(HttpSession session) {
        Long userId = (Long) session.getAttribute(USER_ID_KEY);
        session.invalidate();
        log.debug("Invalidated session for user: {}", userId);
    }

    public void setUserId(HttpSession session, Long id) {
        session.setAttribute(USER_ID_KEY, id);
    }
}


