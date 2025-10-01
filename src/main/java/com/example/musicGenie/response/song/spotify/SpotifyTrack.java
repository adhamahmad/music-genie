package com.example.musicGenie.response.song.spotify;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyTrack {
    private String id;
    private String name;
    private List<SpotifyArtist> artists;
    private SpotifyAlbum album;
    private Integer duration_ms;
    private int popularity;
    private boolean explicit;
}
