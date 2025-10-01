package com.example.musicGenie.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_provider")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProvider {

    @EmbeddedId
    private UserProviderId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("providerId")
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @Column(nullable = false)
    private String providerUserId;

    @Lob
    private String refreshToken;

}
