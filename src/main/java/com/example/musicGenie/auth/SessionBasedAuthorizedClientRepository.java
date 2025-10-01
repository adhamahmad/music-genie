package com.example.musicGenie.auth;

import java.time.Instant;

import com.example.musicGenie.services.provider.ProviderService;
import com.example.musicGenie.services.security.TokenEncryptionService;
import com.example.musicGenie.services.session.SessionService;
import com.example.musicGenie.services.user.UserService;
import com.example.musicGenie.services.userProvider.UserProviderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionBasedAuthorizedClientRepository implements OAuth2AuthorizedClientRepository {

    private final SessionService sessionService;
    private final UserService userService;
    private final ProviderService providerService;
    private final UserProviderService userProviderService;
    private final TokenEncryptionService encryptionService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(
            String clientRegistrationId,
            Authentication principal,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null) return null;

        Long userId = sessionService.getUserId(session);
        if (userId == null) return null;

        String accessToken = sessionService.getAccessToken(session);
        Instant expiry = sessionService.getAccessTokenExpiry(session);

        if (accessToken == null) return null;

        // Get refresh token from database
        Long providerId = providerService.getIdByName(clientRegistrationId);
        String refreshToken = userProviderService.getRefreshTokenDirect(userId, providerId);
        if (refreshToken == null) return null;


        // Create OAuth2 tokens
        OAuth2AccessToken oauth2AccessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                accessToken,
                null, // issued at - can be calculated if needed
                expiry
        );

        OAuth2RefreshToken oauth2RefreshToken = new OAuth2RefreshToken(
                refreshToken,
                null // issued at - can be calculated if needed
        );

        // Get client registration
        ClientRegistration clientRegistration = getClientRegistration(clientRegistrationId);

        return (T) new OAuth2AuthorizedClient(
                clientRegistration,
                getPrincipalName(userId),
                oauth2AccessToken,
                oauth2RefreshToken
        );
    }

    @Override
    public void saveAuthorizedClient(
            OAuth2AuthorizedClient authorizedClient,
            Authentication principal,
            HttpServletRequest request,
            HttpServletResponse response) {

        HttpSession session = request.getSession();
        Long userId = sessionService.getUserId(session);
        String providerName = authorizedClient.getClientRegistration().getRegistrationId();
        Long providerId = providerService.getIdByName(providerName);
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

        // If no userId in session yet, try to get it from the authentication
        if (userId == null) { // first time login
            userId = getUserIdFromAuthentication(principal);
            if (userId == null) {
                log.warn("Cannot save authorized client - no user ID available for provider: {}", providerName);
                return;
            }
            sessionService.createUserSession(session, userId,
                    accessToken.getTokenValue(), accessToken.getExpiresAt());
        }else{
            sessionService.updateAccessToken(session, accessToken.getTokenValue(), accessToken.getExpiresAt());
        }

        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();

        log.debug("Created session for user {} with access token for provider: {}", userId, providerName);

        // Update encrypted refresh token in database if it changed
        if (refreshToken != null) {
            String newRefreshToken = refreshToken.getTokenValue();

            String encryptedNewRefreshToken = encryptionService.encrypt(newRefreshToken);
            userProviderService.saveRefreshToken(userId, providerId, encryptedNewRefreshToken);
            log.debug("Updated refresh token for user: {}", userId);
        }

    }

    @Override
    public void removeAuthorizedClient(
            String clientRegistrationId,
            Authentication principal,
            HttpServletRequest request,
            HttpServletResponse response) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            Long userId = sessionService.getUserId(session);
            Long providerId = providerService.getIdByName(clientRegistrationId);

            if (userId != null) {
                userProviderService.deleteRefreshToken(userId, providerId);
                sessionService.invalidateSession(session);
                log.debug("Removed authorized client for user: {}", userId);
            }
        }
    }

    private Long getUserIdFromAuthentication(Authentication principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
            OAuth2User oauth2User = oauthToken.getPrincipal();
            String email = oauth2User.getAttribute("email");

            if (email != null) {
                try {
                    return userService.getByEmail(email).getId();
                } catch (Exception e) {
                    log.warn("Could not find user by email: {}", email, e);
                }
            }
        }
        return null;
    }

    private ClientRegistration getClientRegistration(String registrationId) {
        return clientRegistrationRepository.findByRegistrationId(registrationId);
    }

    private String getPrincipalName(Long userId) {
        String email = userService.getEmailById(userId);
        return "user_" + userId + "_" + email;
    }
}