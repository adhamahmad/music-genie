package com.example.musicGenie.services.auth;

import java.time.LocalDateTime;

import com.example.musicGenie.auth.OAuth2UserWithRole;
import com.example.musicGenie.dtos.auth.ProviderUserData;
import com.example.musicGenie.enums.Role;
import com.example.musicGenie.mappers.auth.ProviderUserMapper;
import com.example.musicGenie.mappers.auth.ProviderUserMapperFactory;
import com.example.musicGenie.models.Provider;
import com.example.musicGenie.models.User;
import com.example.musicGenie.models.UserProvider;
import com.example.musicGenie.models.UserProviderId;
import com.example.musicGenie.repos.ProviderRepository;
import com.example.musicGenie.repos.UserProviderRepository;
import com.example.musicGenie.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final ProviderUserMapperFactory mapperFactory;
    private final OAuth2AuthenticationService oAuth2AuthenticationService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = loadOAuth2User(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 1️⃣ Get correct mapper for provider
        ProviderUserMapper mapper = mapperFactory.getMapper(registrationId);

        // 2️⃣ Normalize provider data
        ProviderUserData data = mapper.map(oAuth2User, userRequest);

        // Authenticate and persist user data
        User user = oAuth2AuthenticationService.authenticateUser(data, registrationId);

        // 4️⃣ Return OAuth2User with role authorities
        return new OAuth2UserWithRole(oAuth2User, user.getRole());
    }

    // Extracted for testability
    protected OAuth2User loadOAuth2User(OAuth2UserRequest userRequest) {
        return super.loadUser(userRequest);
    }

}