package com.example.musicGenie.services.userProvider;

import com.example.musicGenie.dtos.auth.ProviderUserData;
import com.example.musicGenie.models.Provider;
import com.example.musicGenie.models.User;
import com.example.musicGenie.models.UserProvider;
import com.example.musicGenie.models.UserProviderId;
import com.example.musicGenie.repos.UserProviderRepository;
import com.example.musicGenie.services.security.TokenEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProviderService {

    private final UserProviderRepository userProviderRepository;
    private final TokenEncryptionService encryptionService;

    @Transactional
    public UserProvider linkUserToProvider(User user, Provider provider, ProviderUserData data) {
        UserProviderId upId = new UserProviderId(user.getId(), provider.getId());

        UserProvider userProvider = userProviderRepository.findById(upId)
                                                          .orElseGet(() -> createNewUserProvider(upId, user, provider));

        // Update with fresh data
        userProvider.setProviderUserId(data.getProviderUserId());

        UserProvider saved = userProviderRepository.save(userProvider);
        log.debug("Updated UserProvider for user: {} provider: {}", user.getEmail(), provider.getName());
        return saved;
    }

    @Transactional
    public void updateRefreshToken(User user, Provider provider, String refreshToken) {
        UserProviderId upId = new UserProviderId(user.getId(), provider.getId());
        UserProvider userProvider = userProviderRepository.findById(upId)
                                                          .orElseThrow(() -> new IllegalStateException("UserProvider relationship not found"));

        userProvider.setRefreshToken(encryptionService.encrypt(refreshToken));
        userProviderRepository.save(userProvider);
        log.debug("Updated refresh token for user: {} provider: {}", user.getEmail(), provider.getName());
    }

    @Transactional(readOnly = true)
    public UserProvider getUserProvider(User user, Provider provider) {
        UserProviderId upId = new UserProviderId(user.getId(), provider.getId());
        return userProviderRepository.findById(upId)
                                     .orElseThrow(() -> new IllegalStateException("UserProvider relationship not found"));
    }

    private UserProvider createNewUserProvider(UserProviderId upId, User user, Provider provider) {
        UserProvider userProvider = new UserProvider();
        userProvider.setId(upId);
        userProvider.setUser(user);
        userProvider.setProvider(provider);
        log.info("Creating new UserProvider relationship for user: {} provider: {}",
                user.getEmail(), provider.getName());
        return userProvider;
    }

    @Transactional(readOnly = true)
    public String getRefreshToken(User user, Provider provider) {
        UserProviderId upId = new UserProviderId(user.getId(), provider.getId());
        UserProvider userProvider = userProviderRepository.findById(upId)
                                                          .orElseThrow(() -> new IllegalStateException("UserProvider relationship not found"));

        String encryptedRefreshToken = userProvider.getRefreshToken();
        if (encryptedRefreshToken == null) {
            throw new IllegalStateException("No refresh token stored for user: " + user.getEmail());
        }

        return encryptionService.decrypt(encryptedRefreshToken);
    }


    @Transactional(readOnly = true)
    public String getRefreshTokenDirect(Long userId, Long providerId) {
        return userProviderRepository.findRefreshTokenByIdUserIdAndIdProviderId(userId, providerId)
                                     .map(encryptionService::decrypt)
                                     .orElseThrow(() -> new IllegalStateException(
                                             "No refresh token stored for user: " + userId+ " and provider: " + providerId
                                     ));
    }


    @Transactional
    public void saveRefreshToken(Long userId, Long providerId, String encryptedRefreshToken) {
        userProviderRepository.updateRefreshToken(userId, providerId, encryptedRefreshToken);
        log.debug("Saved refresh token for userId={} providerId={}", userId, providerId);
    }

    @Transactional
    public void deleteRefreshToken(Long userId, Long providerId) {
        userProviderRepository.deleteRefreshToken(userId, providerId);
        log.debug("Deleted refresh token for userId={} providerId={}", userId, providerId);
    }

}
