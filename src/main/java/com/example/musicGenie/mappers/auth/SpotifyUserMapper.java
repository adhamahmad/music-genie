package com.example.musicGenie.mappers.auth;

import com.example.musicGenie.dtos.auth.ProviderUserData;
import com.example.musicGenie.mappers.auth.ProviderUserMapper;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Component("spotifyMapper")
public class SpotifyUserMapper implements ProviderUserMapper {

    @Override
    public ProviderUserData map(OAuth2User oAuth2User, OAuth2UserRequest userRequest) {
        String email = oAuth2User.getAttribute("email");
        String displayName = oAuth2User.getAttribute("display_name");

        String profilePic = null;
        Object imagesObj = oAuth2User.getAttribute("images");

        if (imagesObj instanceof List<?> imagesList && !imagesList.isEmpty()) {
            Object firstImage = imagesList.get(0);

            if (firstImage instanceof Map<?, ?> imageMap) {
                Object urlObj = imageMap.get("url");
                if (urlObj instanceof String) {
                    profilePic = (String) urlObj;
                }
            }
        }


        return ProviderUserData.builder()
                               .email(email)
                               .displayName(displayName)
                               .profilePic(profilePic)
                               .providerUserId(oAuth2User.getName())
                               .accessToken(userRequest.getAccessToken().getTokenValue())
                               .tokenExpiry(userRequest.getAccessToken().getExpiresAt())
                               .build();
    }
}
