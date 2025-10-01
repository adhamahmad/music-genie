package com.example.musicGenie.response.playlist.spotify;

import java.util.List;

import com.example.musicGenie.dtos.playlist.PlaylistDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public
class SpotifyPlaylistItem {
    private String id;
    private String name;
    private SpotifyOwner owner;
    private SpotifyTracks tracks;
    private List<SpotifyImage> images;

    // Convert single playlist to normalized DTO
    public PlaylistDto toDto() {
        return new PlaylistDto(
                id,
                name,
                owner != null ? owner.getDisplayName() : null,
                tracks != null ? tracks.getTotal() : 0,
                (images != null && !images.isEmpty()) ? images.get(0).getUrl() : null
        );
    }
}
