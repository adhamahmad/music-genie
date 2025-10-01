package com.example.musicGenie.response.playlist.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class SpotifyOwner {
    private String display_name;

    public String getDisplayName() {
        return display_name;
    }
}
