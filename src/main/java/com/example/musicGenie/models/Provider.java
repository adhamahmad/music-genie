package com.example.musicGenie.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "providers")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g. "spotify", "apple_music"

    @Column(nullable = false, unique = true)
    private String displayName; // e.g. "Spotify", "Apple Music"
}
