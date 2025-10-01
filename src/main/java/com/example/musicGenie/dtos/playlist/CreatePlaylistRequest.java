package com.example.musicGenie.dtos.playlist;

import lombok.Builder;

@Builder
public record CreatePlaylistRequest(
        String filterId,
        String name
) {}

