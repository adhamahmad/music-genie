package com.example.musicGenie.dtos.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

//@Data
@Builder
@Getter
//@AllArgsConstructor
//@NoArgsConstructor
public class ProviderUserData {
    private String email;
    private String displayName;
    private String profilePic;
    private String providerUserId;
    private String accessToken;
    private Instant tokenExpiry;
}
