package com.example.musicGenie.services.song;

import com.example.musicGenie.dtos.song.SongDto;
import com.example.musicGenie.services.playlist.PlaylistCacheService;
import com.example.musicGenie.song.SongProvider;
import com.example.musicGenie.song.SongProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SongService Tests")
class SongServiceTest {

    @Mock
    private SongProviderFactory providerFactory;

    @Mock
    private PlaylistCacheService playlistCacheService;

    @Mock
    private SongProvider songProvider;

    @InjectMocks
    private SongService songService;

    private static final String PROVIDER_NAME = "spotify";
    private static final String PLAYLIST_ID = "playlist123";
    private static final Long USER_ID = 42L;

    private List<SongDto> mockSongs;

    @BeforeEach
    void setUp() {
        SongDto song1 = SongDto.builder()
                               .id("1")
                               .title("Song One")
                               .artists(Arrays.asList("Artist A"))
                               .album("Album A")
                               .popularity(85)
                               .releaseYear(Year.of(2021))
                               .explicit(false)
                               .addedAt(YearMonth.of(2023, 5))
                               .build();

        SongDto song2 = SongDto.builder()
                               .id("2")
                               .title("Song Two")
                               .artists(Arrays.asList("Artist B", "Artist C"))
                               .album("Album B")
                               .popularity(70)
                               .releaseYear(Year.of(2020))
                               .explicit(true)
                               .addedAt(YearMonth.of(2023, 6))
                               .build();

        mockSongs = Arrays.asList(song1, song2);
    }

    @Test
    @DisplayName("getPlaylistSongs - Should return cached songs when available")
    void getPlaylistSongs_ShouldReturnCached_WhenAvailable() {
        // Given
        when(playlistCacheService.getCachedSongs(USER_ID, PLAYLIST_ID)).thenReturn(mockSongs);

        // When
        List<SongDto> result = songService.getPlaylistSongs(PROVIDER_NAME, PLAYLIST_ID, USER_ID);

        // Then
        assertThat(result).hasSize(2).containsAll(mockSongs);
        verify(playlistCacheService, times(1)).getCachedSongs(USER_ID, PLAYLIST_ID);
        verify(providerFactory, times(1)).getProvider(PROVIDER_NAME);
        verifyNoInteractions(songProvider);
    }

    @Test
    @DisplayName("getPlaylistSongs - Should fetch songs from provider when cache is empty")
    void getPlaylistSongs_ShouldFetchFromProvider_WhenCacheIsEmpty() {
        // Given
        when(playlistCacheService.getCachedSongs(USER_ID, PLAYLIST_ID)).thenReturn(null);
        when(providerFactory.getProvider(PROVIDER_NAME)).thenReturn(songProvider);
        when(songProvider.fetchPlaylistSongs(PLAYLIST_ID)).thenReturn(mockSongs);

        // When
        List<SongDto> result = songService.getPlaylistSongs(PROVIDER_NAME, PLAYLIST_ID, USER_ID);

        // Then
        assertThat(result).hasSize(2).containsAll(mockSongs);
        verify(providerFactory, times(1)).getProvider(PROVIDER_NAME);
        verify(songProvider, times(1)).fetchPlaylistSongs(PLAYLIST_ID);
        verify(playlistCacheService, times(1)).cacheSongs(USER_ID, PLAYLIST_ID, mockSongs);
    }

    @Test
    @DisplayName("getPlaylistSongs - Should handle empty provider response gracefully")
    void getPlaylistSongs_ShouldHandleEmptyProviderResponse() {
        // Given
        when(playlistCacheService.getCachedSongs(USER_ID, PLAYLIST_ID)).thenReturn(null);
        when(providerFactory.getProvider(PROVIDER_NAME)).thenReturn(songProvider);
        when(songProvider.fetchPlaylistSongs(PLAYLIST_ID)).thenReturn(Collections.emptyList());

        // When
        List<SongDto> result = songService.getPlaylistSongs(PROVIDER_NAME, PLAYLIST_ID, USER_ID);

        // Then
        assertThat(result).isEmpty();
        verify(providerFactory, times(1)).getProvider(PROVIDER_NAME);
        verify(songProvider, times(1)).fetchPlaylistSongs(PLAYLIST_ID);
        verify(playlistCacheService, times(1)).cacheSongs(USER_ID, PLAYLIST_ID, Collections.emptyList());
    }
}
