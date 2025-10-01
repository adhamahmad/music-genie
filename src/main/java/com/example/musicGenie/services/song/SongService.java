package com.example.musicGenie.services.song;

import java.util.List;

import com.example.musicGenie.dtos.song.SongDto;
import com.example.musicGenie.services.playlist.PlaylistCacheService;
import com.example.musicGenie.song.SongProvider;
import com.example.musicGenie.song.SongProviderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SongService {
    private final SongProviderFactory providerFactory;
    private final PlaylistCacheService playlistCacheService;

    public List<SongDto> getPlaylistSongs(String providerName, String playlistId, Long userId) {
        SongProvider provider = providerFactory.getProvider(providerName);

        List<SongDto> cached = playlistCacheService.getCachedSongs(userId, playlistId);
        if (cached != null) {
            return cached;
        }

        List<SongDto> songs = provider.fetchPlaylistSongs(playlistId);

        playlistCacheService.cacheSongs(userId, playlistId, songs);

        return  songs;
    }
}
