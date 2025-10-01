package com.example.musicGenie.config.security;

import java.io.IOException;

import com.example.musicGenie.services.auth.OAuth2AuthenticationService;
import com.example.musicGenie.services.session.SessionService;
import com.example.musicGenie.services.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2AuthorizedClientRepository authorizedClientRepository;
    private final OAuth2AuthenticationService oAuth2AuthenticationService;
    private final SessionService sessionService;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauthToken.getPrincipal();

        String providerName = oauthToken.getAuthorizedClientRegistrationId();
        String userEmail = oauth2User.getAttribute("email");

        if (userEmail == null) {
            log.error("Email not found in OAuth2 user attributes for provider: {}", providerName);
            response.sendRedirect("/user/login?error=no-email");
            return;
        }

        try {
            // At this point, the session should already be created by saveAuthorizedClient
            HttpSession session = request.getSession();
            Long userId = sessionService.getUserId(session);

            if (userId == null) {
                System.out.println(" &&&&&&&&&&&&& DEBUG: userId is null → redirecting with error");

                log.error("No user session found after OAuth2 authentication for user: {}", userEmail);
                response.sendRedirect("/user/login?error=no-session");
                return;
            }

            // Load the authorized client to handle refresh token updates
            OAuth2AuthorizedClient client = authorizedClientRepository.loadAuthorizedClient(
                    providerName, authentication, request);

            if (client != null) {
                // Handle refresh token if available
                handleRefreshToken(client, providerName, userEmail);
            }

            log.info("Successfully completed OAuth2 login for user: {} with provider: {}", userEmail, providerName);
            response.sendRedirect("/user/login");

        } catch (Exception e) {
            log.error("OAuth2 login success handling failed for user: {}", userEmail, e);
            response.sendRedirect("/user/login?error=processing-failed");
        }
    }

    private void handleRefreshToken(OAuth2AuthorizedClient client, String providerName, String userEmail) {
        try {
            if (client.getRefreshToken() != null) {
                String refreshToken = client.getRefreshToken().getTokenValue();
                oAuth2AuthenticationService.updateRefreshToken(userEmail, providerName, refreshToken);
                log.debug("Updated refresh token for user: {} provider: {}", userEmail, providerName);
            }
        } catch (Exception e) {
            log.warn("Failed to handle refresh token for user: {} provider: {}", userEmail, providerName, e);
            // Don't fail the entire login process
        }
    }
}