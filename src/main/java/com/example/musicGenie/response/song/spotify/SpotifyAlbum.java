package com.example.musicGenie.response.song.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyAlbum {
    private String name;
    private String release_date;

    public String getReleaseDate() {
        return release_date;
    }
}
