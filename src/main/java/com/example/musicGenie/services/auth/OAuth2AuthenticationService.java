package com.example.musicGenie.services.auth;

import com.example.musicGenie.dtos.auth.ProviderUserData;
import com.example.musicGenie.models.Provider;
import com.example.musicGenie.models.User;
import com.example.musicGenie.services.provider.ProviderService;
import com.example.musicGenie.services.userProvider.UserProviderService;
import com.example.musicGenie.services.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationService {
    private final UserService userService;
    private final ProviderService providerService;
    private final UserProviderService userProviderService;


    @Transactional
    public User authenticateUser(ProviderUserData userData, String providerName) {
        log.debug("Authenticating user: {} with provider: {}", userData.getEmail(), providerName);

        // Validate provider exists
        Provider provider = providerService.getByName(providerName);

        // Find or create user
        User user = userService.findOrCreateUser(userData);

        // Link user to provider with the latest tokens
        userProviderService.linkUserToProvider(user, provider, userData);



        log.info("Successfully authenticated user: {} with provider: {}",
                user.getEmail(), providerName);
        return user;
    }

    @Transactional
    public void updateRefreshToken(String userEmail, String providerName, String refreshToken) {
        try {
            User user = userService.getByEmail(userEmail);
            Provider provider = providerService.getByName(providerName);
            userProviderService.updateRefreshToken(user, provider, refreshToken);
        } catch (Exception e) {
            log.warn("Failed to update refresh token for user: {} provider: {}",
                    userEmail, providerName, e);
            throw e;
        }
    }
}
