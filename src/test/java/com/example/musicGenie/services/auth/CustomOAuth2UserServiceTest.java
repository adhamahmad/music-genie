package com.example.musicGenie.services.auth;

import com.example.musicGenie.auth.OAuth2UserWithRole;
import com.example.musicGenie.dtos.auth.ProviderUserData;
import com.example.musicGenie.enums.Role;
import com.example.musicGenie.mappers.auth.ProviderUserMapper;
import com.example.musicGenie.mappers.auth.ProviderUserMapperFactory;
import com.example.musicGenie.models.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomOAuth2UserService Tests")
class CustomOAuth2UserServiceTest {

    @Mock
    private ProviderUserMapperFactory mapperFactory;

    @Mock
    private ProviderUserMapper providerUserMapper;

    @Mock
    private OAuth2AuthenticationService oAuth2AuthenticationService;

    @Mock
    private OAuth2UserRequest userRequest;

    @Mock
    private OAuth2User oAuth2User;

    @Mock
    private ClientRegistration clientRegistration;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    CustomOAuth2UserService spyService;

    private ProviderUserData testProviderData;
    private User testUser;

    private static final String REGISTRATION_ID = "spotify";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        testProviderData = ProviderUserData.builder()
                                           .email(TEST_EMAIL)
                                           .displayName("Test User")
                                           .profilePic("http://example.com/pic.jpg")
                                           .build();

        testUser = User.builder()
                       .id(1L)
                       .email(TEST_EMAIL)
                       .role(Role.USER)
                       .build();

        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(REGISTRATION_ID);

        // Create a spy of your service so we can stub the super.loadUser call
        spyService = Mockito.spy(customOAuth2UserService);

        // Stub the super.loadUser(...) call only
        doReturn(oAuth2User).when(spyService).loadOAuth2User(any(OAuth2UserRequest.class));
    }

    @Test
    @DisplayName("loadUser - Should map provider data, authenticate user, and return OAuth2UserWithRole")
    void loadUser_ShouldReturnOAuth2UserWithRole() {
        // Given
        when(mapperFactory.getMapper(REGISTRATION_ID)).thenReturn(providerUserMapper);
        when(providerUserMapper.map(oAuth2User, userRequest)).thenReturn(testProviderData);
        when(oAuth2AuthenticationService.authenticateUser(testProviderData, REGISTRATION_ID))
                .thenReturn(testUser);

        // When
        OAuth2User result = spyService.loadUser(userRequest);

        // Then
        assertThat(result).isInstanceOf(OAuth2UserWithRole.class);
        OAuth2UserWithRole userWithRole = (OAuth2UserWithRole) result;
        assertThat(userWithRole.getRole()).isEqualTo(Role.USER);

        verify(mapperFactory, times(1)).getMapper(REGISTRATION_ID);
        verify(providerUserMapper, times(1)).map(oAuth2User, userRequest);
        verify(oAuth2AuthenticationService, times(1)).authenticateUser(testProviderData, REGISTRATION_ID);
    }

    @Test
    @DisplayName("loadUser - Should propagate Role from authenticated User")
    void loadUser_ShouldUseUserRoleFromAuthenticationService() {
        // Given
        User adminUser = testUser.toBuilder().role(Role.ADMIN).build();
        when(mapperFactory.getMapper(REGISTRATION_ID)).thenReturn(providerUserMapper);
        when(providerUserMapper.map(oAuth2User, userRequest)).thenReturn(testProviderData);
        when(oAuth2AuthenticationService.authenticateUser(testProviderData, REGISTRATION_ID))
                .thenReturn(adminUser);

        // When
        OAuth2User result = spyService.loadUser(userRequest);

        // Then
        assertThat(((OAuth2UserWithRole) result).getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("loadUser - Should throw exception when mapper not found")
    void loadUser_ShouldThrowException_WhenMapperNotFound() {

        // Stub the mapper factory to throw exception
        when(mapperFactory.getMapper(REGISTRATION_ID))
                .thenThrow(new IllegalArgumentException("No mapper found"));

        // when / then
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> spyService.loadUser(userRequest) // use spyService here
        );

        // verify
        verify(mapperFactory, times(1)).getMapper(REGISTRATION_ID);
        verifyNoInteractions(oAuth2AuthenticationService);
    }


}
