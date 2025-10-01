package com.example.musicGenie.playlist;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.example.musicGenie.dtos.playlist.PlaylistDto;
import com.example.musicGenie.dtos.song.SongDto;
import com.example.musicGenie.response.playlist.spotify.SpotifyPlaylistItem;
import com.example.musicGenie.response.playlist.spotify.SpotifyPlaylistResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component("spotifyPlaylistProvider")
public class SpotifyPlaylistProvider implements PlaylistProvider{

    private final WebClient webClient;

    public SpotifyPlaylistProvider(Map<String, WebClient> providerWebClients) {
        this.webClient = providerWebClients.get("spotify");
    }
    @Override
    public List<PlaylistDto> fetchUserPlaylists(String accessToken) {
        SpotifyPlaylistResponse response =  webClient.get()
                        .uri("/me/playlists")
                        .retrieve()
                        .bodyToMono(SpotifyPlaylistResponse.class) // map to responseClass
                        .block();

        return response != null ? response.toPlaylistDtos() : Collections.emptyList();
    }

    @Override
    public PlaylistDto fetchUserPlaylist(String accessToken, String playlistId) {
        SpotifyPlaylistItem response = webClient.get()
                                                    .uri("/playlists/{playlistId}", playlistId)
                                                    .retrieve()
                                                    .bodyToMono(SpotifyPlaylistItem.class)
                                                    .block();

        return response != null ? response.toDto() : null;
    }

    public PlaylistDto createPlaylist(String accessToken, String name) {
        Map<String, Object> requestBody = Map.of(
                "name", name,
                "public", true
        );

        // Spotify requires /me/playlists to create new playlists
        SpotifyPlaylistItem response = webClient.post()
                                                .uri("/me/playlists")
                                                .bodyValue(requestBody)
                                                .retrieve()
                                                .bodyToMono(SpotifyPlaylistItem.class)
                                                .block();

        return response != null ? response.toDto() : null;
    }

    @Override
    public void addSongsToPlaylist(String accessToken, String playlistId, List<SongDto> songs) {
        if (songs == null || songs.isEmpty()) return;

        // Convert song IDs to Spotify track URIs
        List<String> uris = songs.stream()
                                 .map(song -> "spotify:track:" + song.getId())
                                 .toList();

        Map<String, Object> requestBody = Map.of("uris", uris);

        webClient.post()
                 .uri("/playlists/{playlistId}/tracks", playlistId)
                 .bodyValue(requestBody)
                 .retrieve()
                 .toBodilessEntity()
                 .block();
    }

}
