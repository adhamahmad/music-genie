package com.example.musicGenie.dtos.playlist;

import lombok.Builder;

@Builder
public record PlaylistDto(
        String id,
        String name,
        String owner,
        int tracksCount,
        String imageUrl
) {}