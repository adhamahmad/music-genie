package com.example.musicGenie.services.playlist;

import java.util.List;

import com.example.musicGenie.dtos.playlist.CreatePlaylistRequest;
import com.example.musicGenie.dtos.playlist.PlaylistDto;
import com.example.musicGenie.dtos.song.SongDto;
import com.example.musicGenie.playlist.PlaylistProvider;
import com.example.musicGenie.playlist.PlaylistProviderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistService {
    private final PlaylistCacheService playlistCacheService;
    private final PlaylistProviderFactory providerFactory;

    public List<PlaylistDto> getUserPlaylists(String providerName, String accessToken, Long userId, boolean forceRefresh) {
        PlaylistProvider provider = providerFactory.getProvider(providerName);

        // try cache first
        if (!forceRefresh) {
            List<PlaylistDto> cached = playlistCacheService.getCachedPlaylistsMetaData(userId);
            if (cached != null) {
                return cached;
            }
        }

        // fetch from provider
//        PlaylistProvider provider = providerFactory.getProvider(providerName);
        List<PlaylistDto> playlists = provider.fetchUserPlaylists(accessToken);

        // cache result
        playlistCacheService.cachePlaylistsMetaData(userId, playlists);

        return playlists;
    }

    public PlaylistDto getPlaylist(String providerName, String accessToken, Long userId, String playlistId){
        PlaylistProvider provider = providerFactory.getProvider(providerName);
        PlaylistDto cached = playlistCacheService.getCachedPlaylistMetaData(userId, playlistId);
        if (cached != null) {
            return cached;
        }

        // fetch from provider
//        PlaylistProvider provider = providerFactory.getProvider(providerName);
        PlaylistDto playlist = provider.fetchUserPlaylist(accessToken, playlistId);

        // cache result
        playlistCacheService.cachePlaylistMetaData(userId, playlist);

        return playlist;

    }

    public PlaylistDto createPlaylistFromFilter(
            String providerName,
            String accessToken,
            Long userId,
            CreatePlaylistRequest request) {

        // get filtered songs from cache
        String filterId = request.filterId();
        List<SongDto> songs = playlistCacheService.getCachedFilteredSongs(userId, filterId);
        if (songs == null || songs.isEmpty()) {
            throw new IllegalStateException("No filtered songs found for filterId " + filterId);
        }

        PlaylistProvider provider = providerFactory.getProvider(providerName);

        // create new playlist on provider
        PlaylistDto newPlaylist = provider.createPlaylist(
                accessToken,
                request.name()
        );

        // add songs
        provider.addSongsToPlaylist(accessToken, newPlaylist.id(), songs);

        // evict cached filter list
        playlistCacheService.evictFilteredSongs(userId, request.filterId());

        return getPlaylist("spotify", accessToken, userId, newPlaylist.id()); // fetch full details
    }


}