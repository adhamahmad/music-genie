package com.example.musicGenie.response.playlist.spotify;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.musicGenie.dtos.playlist.PlaylistDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyPlaylistResponse {
    private List<SpotifyPlaylistItem> items;

    // Mapper to DTOs
    public List<PlaylistDto> toPlaylistDtos() {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                    .map(SpotifyPlaylistItem::toDto)
                    .collect(Collectors.toList());
    }
}
