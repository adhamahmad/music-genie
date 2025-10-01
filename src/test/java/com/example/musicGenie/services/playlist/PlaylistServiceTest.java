package com.example.musicGenie.services.playlist;

import com.example.musicGenie.dtos.playlist.CreatePlaylistRequest;
import com.example.musicGenie.dtos.playlist.PlaylistDto;
import com.example.musicGenie.dtos.song.SongDto;
import com.example.musicGenie.playlist.PlaylistProvider;
import com.example.musicGenie.playlist.PlaylistProviderFactory;
import com.example.musicGenie.services.playlist.PlaylistCacheService;
import com.example.musicGenie.services.playlist.PlaylistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaylistService Tests")
class PlaylistServiceTest {

    @Mock
    private PlaylistCacheService playlistCacheService;

    @Mock
    private PlaylistProviderFactory providerFactory;

    @Mock
    private PlaylistProvider provider;

    @InjectMocks
    private PlaylistService playlistService;

    private static final String PROVIDER_NAME = "spotify";
    private static final String ACCESS_TOKEN = "token123";
    private static final Long USER_ID = 1L;
    private static final String PLAYLIST_ID = "pl123";
    private static final String FILTER_ID = "filter123";

    private PlaylistDto testPlaylist;
    private List<PlaylistDto> testPlaylists;
    private List<SongDto> testSongs;

    @BeforeEach
    void setUp() {
        testPlaylist = PlaylistDto.builder()
                                  .id("123")
                                  .name("Test Vibes")
                                  .owner("Tester123")
                                  .tracksCount(42)
                                  .imageUrl("https://example.com/cover.jpg")
                                  .build();
        testPlaylists = List.of(testPlaylist);

        SongDto song = SongDto.builder()
                              .id("song1")
                              .title("Song 1")
                              .artists(List.of("Artist 1"))
                              .album("Album 1")
                              .popularity(85)
                              .releaseYear(Year.of(2022))
                              .explicit(false)
                              .addedAt(YearMonth.of(2023, 5))
                              .build();

        testSongs = List.of(song);
    }

    @Test
    @DisplayName("getUserPlaylists - Should return cached playlists when available")
    void getUserPlaylists_ShouldReturnCached_WhenAvailable() {
        when(playlistCacheService.getCachedPlaylistsMetaData(USER_ID)).thenReturn(testPlaylists);

        List<PlaylistDto> result = playlistService.getUserPlaylists(PROVIDER_NAME, ACCESS_TOKEN, USER_ID, false);

        assertThat(result).isEqualTo(testPlaylists);
        verify(playlistCacheService, times(1)).getCachedPlaylistsMetaData(USER_ID);
        verify(providerFactory, times(1)).getProvider(PROVIDER_NAME);
    }

    @Test
    @DisplayName("getUserPlaylists - Should fetch and cache playlists when not cached")
    void getUserPlaylists_ShouldFetchAndCache_WhenNotCached() {
        when(playlistCacheService.getCachedPlaylistsMetaData(USER_ID)).thenReturn(null);
        when(providerFactory.getProvider(PROVIDER_NAME)).thenReturn(provider);
        when(provider.fetchUserPlaylists(ACCESS_TOKEN)).thenReturn(testPlaylists);

        List<PlaylistDto> result = playlistService.getUserPlaylists(PROVIDER_NAME, ACCESS_TOKEN, USER_ID, false);

        assertThat(result).isEqualTo(testPlaylists);
        verify(providerFactory, times(1)).getProvider(PROVIDER_NAME);
        verify(provider, times(1)).fetchUserPlaylists(ACCESS_TOKEN);
        verify(playlistCacheService, times(1)).cachePlaylistsMetaData(USER_ID, testPlaylists);
    }

    @Test
    @DisplayName("getPlaylist - Should return cached playlist when available")
    void getPlaylist_ShouldReturnCached_WhenAvailable() {
        when(playlistCacheService.getCachedPlaylistMetaData(USER_ID, PLAYLIST_ID)).thenReturn(testPlaylist);

        PlaylistDto result = playlistService.getPlaylist(PROVIDER_NAME, ACCESS_TOKEN, USER_ID, PLAYLIST_ID);

        assertThat(result).isEqualTo(testPlaylist);
        verify(playlistCacheService, times(1)).getCachedPlaylistMetaData(USER_ID, PLAYLIST_ID);
        verify(providerFactory, times(1)).getProvider(PROVIDER_NAME);
    }

    @Test
    @DisplayName("getPlaylist - Should fetch and cache playlist when not cached")
    void getPlaylist_ShouldFetchAndCache_WhenNotCached() {
        when(playlistCacheService.getCachedPlaylistMetaData(USER_ID, PLAYLIST_ID)).thenReturn(null);
        when(providerFactory.getProvider(PROVIDER_NAME)).thenReturn(provider);
        when(provider.fetchUserPlaylist(ACCESS_TOKEN, PLAYLIST_ID)).thenReturn(testPlaylist);

        PlaylistDto result = playlistService.getPlaylist(PROVIDER_NAME, ACCESS_TOKEN, USER_ID, PLAYLIST_ID);

        assertThat(result).isEqualTo(testPlaylist);
        verify(providerFactory, times(1)).getProvider(PROVIDER_NAME);
        verify(provider, times(1)).fetchUserPlaylist(ACCESS_TOKEN, PLAYLIST_ID);
        verify(playlistCacheService, times(1)).cachePlaylistMetaData(USER_ID, testPlaylist);
    }

    @Test
    @DisplayName("createPlaylistFromFilter - Should throw when no cached songs found")
    void createPlaylistFromFilter_ShouldThrow_WhenNoCachedSongs() {
        CreatePlaylistRequest request = new CreatePlaylistRequest(FILTER_ID, "TEST filter");
        when(playlistCacheService.getCachedFilteredSongs(USER_ID, FILTER_ID)).thenReturn(null);

        assertThatThrownBy(() -> playlistService.createPlaylistFromFilter(PROVIDER_NAME, ACCESS_TOKEN, USER_ID, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No filtered songs found for filterId " + FILTER_ID);

        verify(providerFactory, never()).getProvider(anyString());
    }

    @Test
    @DisplayName("createPlaylistFromFilter - Should create playlist and add songs when cached songs exist")
    void createPlaylistFromFilter_ShouldCreatePlaylistAndAddSongs_WhenSongsExist() {
        CreatePlaylistRequest request = new CreatePlaylistRequest(FILTER_ID, "TEST filter");
        PlaylistDto newPlaylist = PlaylistDto.builder()
                                             .id("123")
                                             .name("Test Vibes")
                                             .owner("Tester123")
                                             .tracksCount(42)
                                             .imageUrl("https://example.com/cover.jpg")
                                             .build();

        when(playlistCacheService.getCachedFilteredSongs(USER_ID, FILTER_ID)).thenReturn(testSongs);
        when(providerFactory.getProvider(PROVIDER_NAME)).thenReturn(provider);
        when(provider.createPlaylist(ACCESS_TOKEN, request.name())).thenReturn(newPlaylist);
        when(provider.fetchUserPlaylist(ACCESS_TOKEN, newPlaylist.id())).thenReturn(newPlaylist);

        PlaylistDto result = playlistService.createPlaylistFromFilter(PROVIDER_NAME, ACCESS_TOKEN, USER_ID, request);

        assertThat(result).isEqualTo(newPlaylist);
        verify(provider, times(1)).addSongsToPlaylist(ACCESS_TOKEN, newPlaylist.id(), testSongs);
        verify(playlistCacheService, times(1)).evictFilteredSongs(USER_ID, FILTER_ID);
    }

}
