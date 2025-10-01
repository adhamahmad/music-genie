package com.example.musicGenie.mappers.auth;

import com.example.musicGenie.dtos.auth.ProviderUserData;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface ProviderUserMapper {
    ProviderUserData map(OAuth2User oAuth2User, OAuth2UserRequest userRequest);
}