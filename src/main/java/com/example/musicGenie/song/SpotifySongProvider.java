package com.example.musicGenie.song;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.musicGenie.dtos.song.SongDto;
import com.example.musicGenie.response.song.spotify.SpotifyPlaylistTracksResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component("spotifySongProvider")
public class SpotifySongProvider implements SongProvider{
    private final WebClient webClient;

    public SpotifySongProvider(Map<String, WebClient> providerWebClients) {
        this.webClient = providerWebClients.get("spotify");
    }


    @Override
    public List<SongDto> fetchPlaylistSongs(String playlistId) {
        List<SongDto> allSongs = new ArrayList<>();
        int limit = 100;
        int offset = 0;
        while(true){
            final int currentOffset = offset; // make a final copy for lambda
            SpotifyPlaylistTracksResponse response = webClient.get()
                                                              .uri(uriBuilder -> uriBuilder
                                                                      .path("/playlists/{playlistId}/tracks")
                                                                      .queryParam("limit", limit)
                                                                      .queryParam("offset", currentOffset)
                                                                      .build(playlistId))
                                                              .retrieve()
                                                              .bodyToMono(SpotifyPlaylistTracksResponse.class)
                                                              .block();

            if (response == null || response.getItems().isEmpty()) break;
            allSongs.addAll(response.toSongDtos());

            if (response.getItems().size() < limit) break; // last page reached
            offset += limit;
        }


        return allSongs;
    }
}
