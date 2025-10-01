package com.example.musicGenie.services.session;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionService Tests")
class SessionServiceTest {

    @Mock
    private HttpSession session;

    @InjectMocks
    private SessionService sessionService;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_ACCESS_TOKEN = "access-token-123";
    private static final Instant FUTURE_EXPIRY = Instant.now().plusSeconds(3600);
    private static final Instant PAST_EXPIRY = Instant.now().minusSeconds(3600);


    @Test
    @DisplayName("createUserSession - Should store userId, token, and expiry in session")
    void createUserSession_ShouldStoreValues() {
        // When
        sessionService.createUserSession(session, TEST_USER_ID, TEST_ACCESS_TOKEN, FUTURE_EXPIRY);

        // Then
        verify(session).setAttribute("USER_ID", TEST_USER_ID);
        verify(session).setAttribute("ACCESS_TOKEN", TEST_ACCESS_TOKEN);
        verify(session).setAttribute("ACCESS_TOKEN_EXPIRY", FUTURE_EXPIRY);
    }

    @Test
    @DisplayName("updateAccessToken - Should update token and expiry in session")
    void updateAccessToken_ShouldUpdateValues() {
        // When
        sessionService.updateAccessToken(session, TEST_ACCESS_TOKEN, FUTURE_EXPIRY);

        // Then
        verify(session).setAttribute("ACCESS_TOKEN", TEST_ACCESS_TOKEN);
        verify(session).setAttribute("ACCESS_TOKEN_EXPIRY", FUTURE_EXPIRY);
    }

    @Test
    @DisplayName("getUserId - Should return stored userId")
    void getUserId_ShouldReturnUserId() {
        // Given
        when(session.getAttribute("USER_ID")).thenReturn(TEST_USER_ID);

        // When
        Long result = sessionService.getUserId(session);

        // Then
        assertThat(result).isEqualTo(TEST_USER_ID);
        verify(session).getAttribute("USER_ID");
    }

    @Test
    @DisplayName("isAuthenticated - Should return true if userId exists")
    void isAuthenticated_ShouldReturnTrue_WhenUserIdExists() {
        // Given
        when(session.getAttribute("USER_ID")).thenReturn(TEST_USER_ID);

        // When
        boolean result = sessionService.isAuthenticated(session);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isAuthenticated - Should return false if userId is null")
    void isAuthenticated_ShouldReturnFalse_WhenUserIdIsNull() {
        // Given
        when(session.getAttribute("USER_ID")).thenReturn(null);

        // When
        boolean result = sessionService.isAuthenticated(session);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("getAccessToken - Should return stored token")
    void getAccessToken_ShouldReturnToken() {
        // Given
        when(session.getAttribute("ACCESS_TOKEN")).thenReturn(TEST_ACCESS_TOKEN);

        // When
        String result = sessionService.getAccessToken(session);

        // Then
        assertThat(result).isEqualTo(TEST_ACCESS_TOKEN);
        verify(session).getAttribute("ACCESS_TOKEN");
    }

    @Test
    @DisplayName("getAccessTokenExpiry - Should return stored expiry")
    void getAccessTokenExpiry_ShouldReturnExpiry() {
        // Given
        when(session.getAttribute("ACCESS_TOKEN_EXPIRY")).thenReturn(FUTURE_EXPIRY);

        // When
        Instant result = sessionService.getAccessTokenExpiry(session);

        // Then
        assertThat(result).isEqualTo(FUTURE_EXPIRY);
        verify(session).getAttribute("ACCESS_TOKEN_EXPIRY");
    }

    @Test
    @DisplayName("isAccessTokenExpired - Should return true if expiry is in past")
    void isAccessTokenExpired_ShouldReturnTrue_WhenExpiryIsPast() {
        // Given
        when(session.getAttribute("ACCESS_TOKEN_EXPIRY")).thenReturn(PAST_EXPIRY);

        // When
        boolean result = sessionService.isAccessTokenExpired(session);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isAccessTokenExpired - Should return false if expiry is in future")
    void isAccessTokenExpired_ShouldReturnFalse_WhenExpiryIsFuture() {
        // Given
        when(session.getAttribute("ACCESS_TOKEN_EXPIRY")).thenReturn(FUTURE_EXPIRY);

        // When
        boolean result = sessionService.isAccessTokenExpired(session);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isAccessTokenExpired - Should return false if expiry is null")
    void isAccessTokenExpired_ShouldReturnFalse_WhenExpiryIsNull() {
        // Given
        when(session.getAttribute("ACCESS_TOKEN_EXPIRY")).thenReturn(null);

        // When
        boolean result = sessionService.isAccessTokenExpired(session);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("invalidateSession - Should invalidate session and log userId")
    void invalidateSession_ShouldInvalidateSession() {
        // Given
        when(session.getAttribute("USER_ID")).thenReturn(TEST_USER_ID);

        // When
        sessionService.invalidateSession(session);

        // Then
        verify(session).getAttribute("USER_ID");
        verify(session).invalidate();
    }

    @Test
    @DisplayName("setUserId - Should set userId in session")
    void setUserId_ShouldSetUserId() {
        // When
        sessionService.setUserId(session, TEST_USER_ID);

        // Then
        verify(session).setAttribute("USER_ID", TEST_USER_ID);
    }
}

