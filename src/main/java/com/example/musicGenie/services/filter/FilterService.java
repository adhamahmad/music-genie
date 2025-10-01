package com.example.musicGenie.services.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.example.musicGenie.dtos.filter.PlaylistFilterRequest;
import com.example.musicGenie.dtos.filter.PlaylistFilterResponse;
import com.example.musicGenie.dtos.song.SongDto;
import com.example.musicGenie.services.playlist.PlaylistCacheService;
import com.example.musicGenie.services.song.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FilterService {
    private final SongService songService;
    private final PlaylistCacheService playlistCacheService;
    public PlaylistFilterResponse filterSongs(Long userId,  PlaylistFilterRequest request) {
        List<String> playlistIds = request.getPlaylistIds();
        List<SongDto> songs = buildSongPool(request.getProvider(),  userId, playlistIds);

        Stream<SongDto> stream = songs.stream();

        if (request.getArtists() != null && !request.getArtists().isEmpty()) {
            stream = stream.filter(song -> song.getArtists().stream()
                                               .anyMatch(a -> request.getArtists().contains(a)));
        }

        if (request.getAlbums() != null && !request.getAlbums().isEmpty()) {
            stream = stream.filter(song -> request.getAlbums().contains(song.getAlbum()));
        }

        if (request.getPopularity() != null) {
            stream = stream.filter(song -> song.getPopularity() <= request.getPopularity());
        }

        if (request.getExplicit() != null) {
            stream = stream.filter(song -> request.getExplicit().equals(song.isExplicit()));
        }

        if (request.getReleaseYear() != null) {
            stream = stream.filter(song ->
                    song.getReleaseYear() != null && song.getReleaseYear().equals(request.getReleaseYear())
            );
        }

        if (request.getAddedAt() != null && "spotify".equalsIgnoreCase(request.getProvider())) {
            stream = stream.filter(song ->
                    song.getAddedAt() != null && (song.getAddedAt().isBefore(request.getAddedAt())
                            || song.getAddedAt().equals(request.getAddedAt()))
            );
        }

        List<SongDto> filteredSongs = stream.toList();
        UUID uuid = playlistCacheService.cacheFilteredSongs(userId, filteredSongs);
        return new PlaylistFilterResponse(uuid,filteredSongs);
    }

    public List<SongDto> buildSongPool(String providerName, Long userId , List<String> playlistIds){
        HashSet<SongDto> songs = new HashSet<>();
        for(String playlistId : playlistIds){
            songs.addAll(songService.getPlaylistSongs(providerName, playlistId, userId));
        }
        return new ArrayList<>(songs);
    }
}
