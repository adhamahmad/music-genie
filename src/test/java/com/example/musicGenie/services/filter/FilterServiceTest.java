package com.example.musicGenie.services.filter;

import com.example.musicGenie.dtos.filter.PlaylistFilterRequest;
import com.example.musicGenie.dtos.filter.PlaylistFilterResponse;
import com.example.musicGenie.dtos.song.SongDto;
import com.example.musicGenie.services.playlist.PlaylistCacheService;
import com.example.musicGenie.services.song.SongService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FilterService Tests")
class FilterServiceTest {

    @Mock
    private SongService songService;

    @Mock
    private PlaylistCacheService playlistCacheService;

    @InjectMocks
    private FilterService filterService;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_PLAYLIST_ID = "playlist123";
    private static final String PROVIDER = "spotify";

    private SongDto song1;
    private SongDto song2;
    private SongDto song3;

    private UUID cachedId;

    @BeforeEach
    void setUp() {
        song1 = SongDto.builder()
                       .id("1")
                       .album("Album A")
                       .artists(List.of("Artist1"))
                       .popularity(50)
                       .explicit(true)
                       .releaseYear(Year.of(2020))
                       .addedAt(YearMonth.from(LocalDate.of(2021, 1, 1)))
                       .build();

        song2 = SongDto.builder()
                       .id("2")
                       .album("Album B")
                       .artists(List.of("Artist2", "Artist3"))
                       .popularity(80)
                       .explicit(false)
                       .releaseYear(Year.of(2021))
                       .addedAt(YearMonth.from(LocalDate.of(2022, 5, 1)))
                       .build();

        song3 = SongDto.builder()
                       .id("3")
                       .album("Album A")
                       .artists(List.of("Artist1", "Artist4"))
                       .popularity(30)
                       .explicit(false)
                       .releaseYear(Year.of(2020))
                       .addedAt(YearMonth.from(LocalDate.of(2021, 1, 1)))
                       .build();

        cachedId = UUID.randomUUID();
        lenient().when(playlistCacheService.cacheFilteredSongs(anyLong(), anyList())).thenReturn(cachedId);
    }

    @Test
    @DisplayName("filterSongs - Should return all songs when no filters applied")
    void filterSongs_ShouldReturnAllSongs_WhenNoFilters() {
        PlaylistFilterRequest request = PlaylistFilterRequest.builder()
                                                             .playlistIds(List.of(TEST_PLAYLIST_ID))
                                                             .provider(PROVIDER)
                                                             .build();

        when(songService.getPlaylistSongs(PROVIDER, TEST_PLAYLIST_ID, TEST_USER_ID))
                .thenReturn(List.of(song1, song2, song3));

        PlaylistFilterResponse response = filterService.filterSongs(TEST_USER_ID, request);

        assertThat(response.getSongs()).containsExactlyInAnyOrder(song1, song2, song3);
        assertThat(response.getFilterId()).isEqualTo(cachedId);
        verify(playlistCacheService).cacheFilteredSongs(TEST_USER_ID, response.getSongs());
    }

    @Test
    @DisplayName("filterSongs - Should filter by artists")
    void filterSongs_ShouldFilterByArtists() {
        PlaylistFilterRequest request = PlaylistFilterRequest.builder()
                                                             .playlistIds(List.of(TEST_PLAYLIST_ID))
                                                             .provider(PROVIDER)
                                                             .artists(List.of("Artist1"))
                                                             .build();

        when(songService.getPlaylistSongs(PROVIDER, TEST_PLAYLIST_ID, TEST_USER_ID))
                .thenReturn(List.of(song1, song2, song3));

        PlaylistFilterResponse response = filterService.filterSongs(TEST_USER_ID, request);

        assertThat(response.getSongs()).containsExactlyInAnyOrder(song1, song3);
    }

    @Test
    @DisplayName("filterSongs - Should filter by album")
    void filterSongs_ShouldFilterByAlbum() {
        PlaylistFilterRequest request = PlaylistFilterRequest.builder()
                                                             .playlistIds(List.of(TEST_PLAYLIST_ID))
                                                             .provider(PROVIDER)
                                                             .albums(List.of("Album B"))
                                                             .build();

        when(songService.getPlaylistSongs(anyString(), anyString(), anyLong()))
                .thenReturn(List.of(song1, song2));

        PlaylistFilterResponse response = filterService.filterSongs(TEST_USER_ID, request);

        assertThat(response.getSongs()).containsExactly(song2);
    }

    @Test
    @DisplayName("filterSongs - Should filter by popularity")
    void filterSongs_ShouldFilterByPopularity() {
        PlaylistFilterRequest request = PlaylistFilterRequest.builder()
                                                             .playlistIds(List.of(TEST_PLAYLIST_ID))
                                                             .provider(PROVIDER)
                                                             .popularity(50)
                                                             .build();

        when(songService.getPlaylistSongs(anyString(), anyString(), anyLong()))
                .thenReturn(List.of(song1, song2, song3));

        PlaylistFilterResponse response = filterService.filterSongs(TEST_USER_ID, request);

        assertThat(response.getSongs()).containsExactlyInAnyOrder(song1, song3);
    }

    @Test
    @DisplayName("filterSongs - Should filter by explicit flag")
    void filterSongs_ShouldFilterByExplicit() {
        PlaylistFilterRequest request = PlaylistFilterRequest.builder()
                                                             .playlistIds(List.of(TEST_PLAYLIST_ID))
                                                             .provider(PROVIDER)
                                                             .explicit(false)
                                                             .build();

        when(songService.getPlaylistSongs(anyString(), anyString(), anyLong()))
                .thenReturn(List.of(song1, song2, song3));

        PlaylistFilterResponse response = filterService.filterSongs(TEST_USER_ID, request);

        assertThat(response.getSongs()).containsExactlyInAnyOrder(song2, song3);
    }

    @Test
    @DisplayName("filterSongs - Should filter by release year")
    void filterSongs_ShouldFilterByReleaseYear() {
        PlaylistFilterRequest request = PlaylistFilterRequest.builder()
                                                             .playlistIds(List.of(TEST_PLAYLIST_ID))
                                                             .provider(PROVIDER)
                                                             .releaseYear(Year.of(2020))
                                                             .build();

        when(songService.getPlaylistSongs(anyString(), anyString(), anyLong()))
                .thenReturn(List.of(song1, song2, song3));

        PlaylistFilterResponse response = filterService.filterSongs(TEST_USER_ID, request);

        assertThat(response.getSongs()).containsExactlyInAnyOrder(song1, song3);
    }

    @Test
    @DisplayName("filterSongs - Should filter by addedAt for Spotify")
    void filterSongs_ShouldFilterByAddedAt_WhenSpotify() {
        PlaylistFilterRequest request = PlaylistFilterRequest.builder()
                                                             .playlistIds(List.of(TEST_PLAYLIST_ID))
                                                             .provider("spotify")
                                                             .addedAt(YearMonth.from(LocalDate.of(2021, 1, 1)))
                                                             .build();

        when(songService.getPlaylistSongs(anyString(), anyString(), anyLong()))
                .thenReturn(List.of(song1, song2, song3));

        PlaylistFilterResponse response = filterService.filterSongs(TEST_USER_ID, request);

        assertThat(response.getSongs()).containsExactlyInAnyOrder(song1, song3);
    }

    @Test
    @DisplayName("buildSongPool - Should collect songs from multiple playlists without duplicates")
    void buildSongPool_ShouldReturnUniqueSongs() {
        when(songService.getPlaylistSongs(PROVIDER, "p1", TEST_USER_ID)).thenReturn(List.of(song1, song2));
        when(songService.getPlaylistSongs(PROVIDER, "p2", TEST_USER_ID)).thenReturn(List.of(song2, song3));

        List<SongDto> result = filterService.buildSongPool(PROVIDER, TEST_USER_ID, List.of("p1", "p2"));

        assertThat(result).containsExactlyInAnyOrder(song1, song2, song3);
    }
}
