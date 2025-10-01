package com.example.musicGenie.response.song.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyPlaylistTrackItem {
    private SpotifyTrack track;
    private String added_at;

    public String getAddedAt() {
        return added_at;
    }
}
