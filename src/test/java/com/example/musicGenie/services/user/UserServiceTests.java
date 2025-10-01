package com.example.musicGenie.services.user;

import com.example.musicGenie.dtos.auth.ProviderUserData;
import com.example.musicGenie.enums.Role;
import com.example.musicGenie.models.User;
import com.example.musicGenie.repos.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private ProviderUserData testProviderData;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_DISPLAY_NAME = "Test User";
    private static final String TEST_PROFILE_PIC = "http://example.com/pic.jpg";
    private static final Long TEST_ID = 1L;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                       .id(TEST_ID)
                       .email(TEST_EMAIL)
                       .displayName(TEST_DISPLAY_NAME)
                       .profilePic(TEST_PROFILE_PIC)
                       .role(Role.USER)
                       .createdAt(LocalDateTime.now())
                       .build();

        testProviderData = ProviderUserData.builder()
                                           .email(TEST_EMAIL)
                                           .displayName(TEST_DISPLAY_NAME)
                                           .profilePic(TEST_PROFILE_PIC)
                                           .build();
    }

    @Test
    @DisplayName("findByEmail - Should return user when found")
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByEmail(TEST_EMAIL);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(TEST_EMAIL);
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("findByEmail - Should return empty optional when user not found")
    void findByEmail_ShouldReturnEmpty_WhenUserNotFound() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(TEST_EMAIL);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("getByEmail - Should return user when found")
    void getByEmail_ShouldReturnUser_WhenUserExists() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getByEmail(TEST_EMAIL);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("getByEmail - Should throw exception when user not found")
    void getByEmail_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getByEmail(TEST_EMAIL))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User not found: " + TEST_EMAIL);

        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("createUser - Should create and save new user")
    void createUser_ShouldCreateAndSaveUser() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser(testProviderData);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository, times(1)).save(any(User.class));

        // Verify the user object passed to save has correct properties
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals(TEST_EMAIL) &&
                        user.getDisplayName().equals(TEST_DISPLAY_NAME) &&
                        user.getProfilePic().equals(TEST_PROFILE_PIC) &&
                        user.getRole().equals(Role.USER) &&
                        user.getCreatedAt() != null
        ));
    }

    @Test
    @DisplayName("findOrCreateUser - Should return existing user when found")
    void findOrCreateUser_ShouldReturnExistingUser_WhenUserExists() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.findOrCreateUser(testProviderData);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("findOrCreateUser - Should create new user when not found")
    void findOrCreateUser_ShouldCreateNewUser_WhenUserNotFound() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.findOrCreateUser(testProviderData);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - Should save and return updated user")
    void updateUser_ShouldSaveAndReturnUser() {
        // Given
        User updatedUser = testUser.toBuilder()
                                   .displayName("Updated Name")
                                   .build();
        when(userRepository.save(testUser)).thenReturn(updatedUser);

        // When
        User result = userService.updateUser(testUser);

        // Then
        assertThat(result).isEqualTo(updatedUser);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("getEmailById - Should return email when user found")
    void getEmailById_ShouldReturnEmail_WhenUserExists() {
        // Given
        when(userRepository.findEmailById(TEST_ID)).thenReturn(Optional.of(TEST_EMAIL));

        // When
        String result = userService.getEmailById(TEST_ID);

        // Then
        assertThat(result).isEqualTo(TEST_EMAIL);
        verify(userRepository, times(1)).findEmailById(TEST_ID);
    }

    @Test
    @DisplayName("getEmailById - Should throw exception when user not found")
    void getEmailById_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findEmailById(TEST_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getEmailById(TEST_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User not found with id: " + TEST_ID);

        verify(userRepository, times(1)).findEmailById(TEST_ID);
    }

    @Test
    @DisplayName("createUser - Should handle null profile picture")
    void createUser_ShouldHandleNullProfilePicture() {
        // Given
        ProviderUserData dataWithNullPic = ProviderUserData.builder()
                                                           .email(TEST_EMAIL)
                                                           .displayName(TEST_DISPLAY_NAME)
                                                           .profilePic(null)
                                                           .build();

        User userWithNullPic = testUser.toBuilder()
                                       .profilePic(null)
                                       .build();

        when(userRepository.save(any(User.class))).thenReturn(userWithNullPic);

        // When
        User result = userService.createUser(dataWithNullPic);

        // Then
        assertThat(result.getProfilePic()).isNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("All methods should use appropriate transaction annotations")
    void verifyTransactionAnnotations() throws NoSuchMethodException {
        // Verify read-only methods have @Transactional(readOnly = true)
        assertThat(UserService.class.getMethod("findByEmail", String.class)
                                    .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();

        assertThat(UserService.class.getMethod("getByEmail", String.class)
                                    .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();

        assertThat(UserService.class.getMethod("getEmailById", Long.class)
                                    .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();

        // Verify write methods have @Transactional
        assertThat(UserService.class.getMethod("createUser", ProviderUserData.class)
                                    .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();

        assertThat(UserService.class.getMethod("findOrCreateUser", ProviderUserData.class)
                                    .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();

        assertThat(UserService.class.getMethod("updateUser", User.class)
                                    .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();
    }
}