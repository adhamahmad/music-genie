package com.example.musicGenie.response.playlist.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class SpotifyImage {
    private String url;
}

