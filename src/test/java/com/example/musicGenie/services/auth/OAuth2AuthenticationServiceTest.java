package com.example.musicGenie.services.auth;

import com.example.musicGenie.dtos.auth.ProviderUserData;
import com.example.musicGenie.models.Provider;
import com.example.musicGenie.models.User;
import com.example.musicGenie.services.provider.ProviderService;
import com.example.musicGenie.services.user.UserService;
import com.example.musicGenie.services.userProvider.UserProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2AuthenticationService Tests")
class OAuth2AuthenticationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ProviderService providerService;

    @Mock
    private UserProviderService userProviderService;

    @InjectMocks
    private OAuth2AuthenticationService authenticationService;

    private ProviderUserData testUserData;
    private User testUser;
    private Provider testProvider;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_DISPLAY_NAME = "Test User";
    private static final String TEST_PROVIDER_NAME = "spotify";
    private static final String TEST_REFRESH_TOKEN = "refresh-token-123";

    @BeforeEach
    void setUp() {
        testUserData = ProviderUserData.builder()
                                       .email(TEST_EMAIL)
                                       .displayName(TEST_DISPLAY_NAME)
                                       .profilePic("http://example.com/pic.jpg")
                                       .build();

        testUser = User.builder()
                       .id(1L)
                       .email(TEST_EMAIL)
                       .displayName(TEST_DISPLAY_NAME)
                       .build();

        testProvider = Provider.builder()
                               .id(10L)
                               .name(TEST_PROVIDER_NAME)
                               .build();
    }

    @Test
    @DisplayName("authenticateUser - Should authenticate and return user successfully")
    void authenticateUser_ShouldReturnUser_WhenSuccessful() {
        // Given
        when(providerService.getByName(TEST_PROVIDER_NAME)).thenReturn(testProvider);
        when(userService.findOrCreateUser(testUserData)).thenReturn(testUser);

        // When
        User result = authenticationService.authenticateUser(testUserData, TEST_PROVIDER_NAME);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(providerService, times(1)).getByName(TEST_PROVIDER_NAME);
        verify(userService, times(1)).findOrCreateUser(testUserData);
        verify(userProviderService, times(1)).linkUserToProvider(testUser, testProvider, testUserData);
    }

    @Test
    @DisplayName("authenticateUser - Should throw when provider not found")
    void authenticateUser_ShouldThrow_WhenProviderNotFound() {
        // Given
        when(providerService.getByName(TEST_PROVIDER_NAME)).thenThrow(new IllegalStateException("Provider not found"));

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticateUser(testUserData, TEST_PROVIDER_NAME))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Provider not found");

        verify(providerService, times(1)).getByName(TEST_PROVIDER_NAME);
        verify(userService, never()).findOrCreateUser(any());
        verify(userProviderService, never()).linkUserToProvider(any(), any(), any());
    }

    @Test
    @DisplayName("updateRefreshToken - Should update refresh token successfully")
    void updateRefreshToken_ShouldUpdateToken_WhenSuccessful() {
        // Given
        when(userService.getByEmail(TEST_EMAIL)).thenReturn(testUser);
        when(providerService.getByName(TEST_PROVIDER_NAME)).thenReturn(testProvider);

        // When
        authenticationService.updateRefreshToken(TEST_EMAIL, TEST_PROVIDER_NAME, TEST_REFRESH_TOKEN);

        // Then
        verify(userService, times(1)).getByEmail(TEST_EMAIL);
        verify(providerService, times(1)).getByName(TEST_PROVIDER_NAME);
        verify(userProviderService, times(1)).updateRefreshToken(testUser, testProvider, TEST_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("updateRefreshToken - Should rethrow when user not found")
    void updateRefreshToken_ShouldRethrow_WhenUserNotFound() {
        // Given
        when(userService.getByEmail(TEST_EMAIL)).thenThrow(new IllegalStateException("User not found"));

        // When & Then
        assertThatThrownBy(() ->
                authenticationService.updateRefreshToken(TEST_EMAIL, TEST_PROVIDER_NAME, TEST_REFRESH_TOKEN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User not found");

        verify(userService, times(1)).getByEmail(TEST_EMAIL);
        verify(providerService, never()).getByName(anyString());
        verify(userProviderService, never()).updateRefreshToken(any(), any(), any());
    }

    @Test
    @DisplayName("updateRefreshToken - Should rethrow when provider not found")
    void updateRefreshToken_ShouldRethrow_WhenProviderNotFound() {
        // Given
        when(userService.getByEmail(TEST_EMAIL)).thenReturn(testUser);
        when(providerService.getByName(TEST_PROVIDER_NAME)).thenThrow(new IllegalStateException("Provider not found"));

        // When & Then
        assertThatThrownBy(() ->
                authenticationService.updateRefreshToken(TEST_EMAIL, TEST_PROVIDER_NAME, TEST_REFRESH_TOKEN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Provider not found");

        verify(userService, times(1)).getByEmail(TEST_EMAIL);
        verify(providerService, times(1)).getByName(TEST_PROVIDER_NAME);
        verify(userProviderService, never()).updateRefreshToken(any(), any(), any());
    }

    @Test
    @DisplayName("All service methods should have @Transactional annotation")
    void verifyTransactionalAnnotations() throws NoSuchMethodException {
        assertThat(OAuth2AuthenticationService.class
                .getMethod("authenticateUser", ProviderUserData.class, String.class)
                .isAnnotationPresent(Transactional.class))
                .isTrue();

        assertThat(OAuth2AuthenticationService.class
                .getMethod("updateRefreshToken", String.class, String.class, String.class)
                .isAnnotationPresent(Transactional.class))
                .isTrue();
    }
}

