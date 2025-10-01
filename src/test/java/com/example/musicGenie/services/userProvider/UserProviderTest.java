package com.example.musicGenie.services.userProvider;


import com.example.musicGenie.dtos.auth.ProviderUserData;
import com.example.musicGenie.models.Provider;
import com.example.musicGenie.models.User;
import com.example.musicGenie.models.UserProvider;
import com.example.musicGenie.models.UserProviderId;
import com.example.musicGenie.repos.UserProviderRepository;
import com.example.musicGenie.services.security.TokenEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProviderService Tests")
class UserProviderServiceTest {

    @Mock
    private UserProviderRepository userProviderRepository;

    @Mock
    private TokenEncryptionService encryptionService;

    @InjectMocks
    private UserProviderService userProviderService;

    private User testUser;
    private Provider testProvider;
    private ProviderUserData testProviderData;
    private UserProviderId testId;
    private UserProvider testUserProvider;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_PROVIDER_ID = 10L;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PROVIDER_NAME = "Spotify";
    private static final String TEST_REFRESH_TOKEN = "refresh123";
    private static final String ENCRYPTED_TOKEN = "encrypted123";

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(TEST_USER_ID).email(TEST_EMAIL).build();
        testProvider = Provider.builder().id(TEST_PROVIDER_ID).name(TEST_PROVIDER_NAME).build();
        testProviderData = ProviderUserData.builder().providerUserId("provider-123").build();
        testId = new UserProviderId(TEST_USER_ID, TEST_PROVIDER_ID);

        testUserProvider = new UserProvider();
        testUserProvider.setId(testId);
        testUserProvider.setUser(testUser);
        testUserProvider.setProvider(testProvider);
        testUserProvider.setProviderUserId("provider-123");
    }

    @Test
    @DisplayName("linkUserToProvider - Should update existing UserProvider")
    void linkUserToProvider_ShouldUpdateExistingUserProvider() {
        // Given
        when(userProviderRepository.findById(testId)).thenReturn(Optional.of(testUserProvider));
        when(userProviderRepository.save(any(UserProvider.class))).thenReturn(testUserProvider);

        // When
        UserProvider result = userProviderService.linkUserToProvider(testUser, testProvider, testProviderData);

        // Then
        assertThat(result.getProviderUserId()).isEqualTo(testProviderData.getProviderUserId());
        verify(userProviderRepository).save(testUserProvider);
    }

    @Test
    @DisplayName("linkUserToProvider - Should create new UserProvider if not exists")
    void linkUserToProvider_ShouldCreateNewUserProvider_WhenNotFound() {
        // Given
        when(userProviderRepository.findById(testId)).thenReturn(Optional.empty());
        when(userProviderRepository.save(any(UserProvider.class))).thenReturn(testUserProvider);

        // When
        UserProvider result = userProviderService.linkUserToProvider(testUser, testProvider, testProviderData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getProvider()).isEqualTo(testProvider);
        verify(userProviderRepository).save(any(UserProvider.class));
    }

    @Test
    @DisplayName("updateRefreshToken - Should encrypt and save refresh token")
    void updateRefreshToken_ShouldEncryptAndSaveToken() {
        // Given
        when(userProviderRepository.findById(testId)).thenReturn(Optional.of(testUserProvider));
        when(encryptionService.encrypt(TEST_REFRESH_TOKEN)).thenReturn(ENCRYPTED_TOKEN);

        // When
        userProviderService.updateRefreshToken(testUser, testProvider, TEST_REFRESH_TOKEN);

        // Then
        assertThat(testUserProvider.getRefreshToken()).isEqualTo(ENCRYPTED_TOKEN);
        verify(userProviderRepository).save(testUserProvider);
    }

    @Test
    @DisplayName("updateRefreshToken - Should throw exception when UserProvider not found")
    void updateRefreshToken_ShouldThrow_WhenNotFound() {
        // Given
        when(userProviderRepository.findById(testId)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> userProviderService.updateRefreshToken(testUser, testProvider, TEST_REFRESH_TOKEN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UserProvider relationship not found");
    }

    @Test
    @DisplayName("getUserProvider - Should return existing UserProvider")
    void getUserProvider_ShouldReturnUserProvider_WhenExists() {
        // Given
        when(userProviderRepository.findById(testId)).thenReturn(Optional.of(testUserProvider));

        // When
        UserProvider result = userProviderService.getUserProvider(testUser, testProvider);

        // Then
        assertThat(result).isEqualTo(testUserProvider);
    }

    @Test
    @DisplayName("getUserProvider - Should throw when UserProvider not found")
    void getUserProvider_ShouldThrow_WhenNotFound() {
        // Given
        when(userProviderRepository.findById(testId)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> userProviderService.getUserProvider(testUser, testProvider))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UserProvider relationship not found");
    }

    @Test
    @DisplayName("getRefreshToken - Should decrypt stored token")
    void getRefreshToken_ShouldDecryptStoredToken() {
        // Given
        testUserProvider.setRefreshToken(ENCRYPTED_TOKEN);
        when(userProviderRepository.findById(testId)).thenReturn(Optional.of(testUserProvider));
        when(encryptionService.decrypt(ENCRYPTED_TOKEN)).thenReturn(TEST_REFRESH_TOKEN);

        // When
        String result = userProviderService.getRefreshToken(testUser, testProvider);

        // Then
        assertThat(result).isEqualTo(TEST_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("getRefreshToken - Should throw when no token stored")
    void getRefreshToken_ShouldThrow_WhenNoTokenStored() {
        // Given
        testUserProvider.setRefreshToken(null);
        when(userProviderRepository.findById(testId)).thenReturn(Optional.of(testUserProvider));

        // Then
        assertThatThrownBy(() -> userProviderService.getRefreshToken(testUser, testProvider))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No refresh token stored for user");
    }

    @Test
    @DisplayName("getRefreshTokenDirect - Should decrypt refresh token")
    void getRefreshTokenDirect_ShouldDecryptToken() {
        // Given
        when(userProviderRepository.findRefreshTokenByIdUserIdAndIdProviderId(TEST_USER_ID, TEST_PROVIDER_ID))
                .thenReturn(Optional.of(ENCRYPTED_TOKEN));
        when(encryptionService.decrypt(ENCRYPTED_TOKEN)).thenReturn(TEST_REFRESH_TOKEN);

        // When
        String result = userProviderService.getRefreshTokenDirect(TEST_USER_ID, TEST_PROVIDER_ID);

        // Then
        assertThat(result).isEqualTo(TEST_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("getRefreshTokenDirect - Should throw when token not found")
    void getRefreshTokenDirect_ShouldThrow_WhenNotFound() {
        // Given
        when(userProviderRepository.findRefreshTokenByIdUserIdAndIdProviderId(TEST_USER_ID, TEST_PROVIDER_ID))
                .thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> userProviderService.getRefreshTokenDirect(TEST_USER_ID, TEST_PROVIDER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No refresh token stored for user");
    }

    @Test
    @DisplayName("saveRefreshToken - Should update refresh token in repository")
    void saveRefreshToken_ShouldUpdateRefreshToken() {
        // When
        userProviderService.saveRefreshToken(TEST_USER_ID, TEST_PROVIDER_ID, ENCRYPTED_TOKEN);

        // Then
        verify(userProviderRepository).updateRefreshToken(TEST_USER_ID, TEST_PROVIDER_ID, ENCRYPTED_TOKEN);
    }

    @Test
    @DisplayName("deleteRefreshToken - Should delete refresh token from repository")
    void deleteRefreshToken_ShouldDeleteRefreshToken() {
        // When
        userProviderService.deleteRefreshToken(TEST_USER_ID, TEST_PROVIDER_ID);

        // Then
        verify(userProviderRepository).deleteRefreshToken(TEST_USER_ID, TEST_PROVIDER_ID);
    }

    @Test
    @DisplayName("All methods should use appropriate transaction annotations")
    void verifyTransactionAnnotations() throws NoSuchMethodException {
        // Verify @Transactional present on write methods
        assertThat(UserProviderService.class.getMethod("linkUserToProvider", User.class, Provider.class, ProviderUserData.class)
                                            .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();

        assertThat(UserProviderService.class.getMethod("updateRefreshToken", User.class, Provider.class, String.class)
                                            .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();

        assertThat(UserProviderService.class.getMethod("saveRefreshToken", Long.class, Long.class, String.class)
                                            .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();

        assertThat(UserProviderService.class.getMethod("deleteRefreshToken", Long.class, Long.class)
                                            .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();

        // Verify read-only methods
        assertThat(UserProviderService.class.getMethod("getUserProvider", User.class, Provider.class)
                                            .getAnnotation(org.springframework.transaction.annotation.Transactional.class).readOnly())
                .isTrue();

        assertThat(UserProviderService.class.getMethod("getRefreshToken", User.class, Provider.class)
                                            .getAnnotation(org.springframework.transaction.annotation.Transactional.class).readOnly())
                .isTrue();

        assertThat(UserProviderService.class.getMethod("getRefreshTokenDirect", Long.class, Long.class)
                                            .getAnnotation(org.springframework.transaction.annotation.Transactional.class).readOnly())
                .isTrue();
    }

}
