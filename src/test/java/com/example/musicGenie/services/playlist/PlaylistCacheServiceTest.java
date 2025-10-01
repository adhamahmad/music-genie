package com.example.musicGenie.services.playlist;

import com.example.musicGenie.dtos.playlist.PlaylistDto;
import com.example.musicGenie.dtos.song.SongDto;
import com.example.musicGenie.services.playlist.PlaylistCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Year;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaylistCacheService Tests")
class PlaylistCacheServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private PlaylistCacheService playlistCacheService;

    private static final Long USER_ID = 1L;
    private static final String PLAYLIST_ID = "playlist-123";
    private static final String FILTER_ID = "filter-123";

    private SongDto testSong;
    private List<SongDto> songList;
    private PlaylistDto testPlaylist;

    @BeforeEach
    void setUp() {
        testSong = SongDto.builder()
                          .id("1")
                          .title("Song One")
                          .artists(Arrays.asList("Artist A"))
                          .album("Album A")
                          .popularity(85)
                          .releaseYear(Year.of(2021))
                          .explicit(false)
                          .addedAt(YearMonth.of(2023, 5))
                          .build();
        songList = List.of(testSong);
        testPlaylist = PlaylistDto.builder()
                                  .id("123")
                                  .name("Test Vibes")
                                  .owner("Tester123")
                                  .tracksCount(42)
                                  .imageUrl("https://example.com/cover.jpg")
                                  .build();
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ---- FILTERED SONGS ----

    @Test
    @DisplayName("cacheFilteredSongs - Should serialize and cache songs")
    void cacheFilteredSongs_ShouldSerializeAndCacheSongs() throws Exception {
        when(objectMapper.writeValueAsString(songList)).thenReturn("json");

        playlistCacheService.cacheFilteredSongs(USER_ID, songList);

        verify(valueOperations).set(startsWith("user:filtered:" + USER_ID), eq("json"), any());
    }

    @Test
    @DisplayName("getCachedFilteredSongs - Should return deserialized songs when found")
    void getCachedFilteredSongs_ShouldReturnSongs_WhenFound() throws Exception {
        when(valueOperations.get("user:filtered:" + USER_ID + ":" + FILTER_ID + ":songs")).thenReturn("json");
        when(objectMapper.readValue(eq("json"), any(TypeReference.class))).thenReturn(songList);

        List<SongDto> result = playlistCacheService.getCachedFilteredSongs(USER_ID, FILTER_ID);

        assertThat(result).containsExactly(testSong);
    }

    @Test
    @DisplayName("getCachedFilteredSongs - Should return null when not found")
    void getCachedFilteredSongs_ShouldReturnNull_WhenNotFound() {
        when(valueOperations.get(anyString())).thenReturn(null);

        List<SongDto> result = playlistCacheService.getCachedFilteredSongs(USER_ID, FILTER_ID);

        assertThat(result).isNull();
    }
//
    @Test
    @DisplayName("evictFilteredSongs - Should delete filtered songs cache")
    void evictFilteredSongs_ShouldDeleteCache() {
        playlistCacheService.evictFilteredSongs(USER_ID, FILTER_ID);

        verify(redisTemplate).delete("user:filtered:" + USER_ID + ":" + FILTER_ID + ":songs");
    }
//
//    // ---- SONGS ----
//
    @Test
    @DisplayName("cacheSongs - Should serialize and cache songs")
    void cacheSongs_ShouldSerializeAndCacheSongs() throws Exception {
        when(objectMapper.writeValueAsString(songList)).thenReturn("json");

        playlistCacheService.cacheSongs(USER_ID, PLAYLIST_ID, songList);

        verify(valueOperations).set(eq("user:songs:" + USER_ID + ":" + PLAYLIST_ID + ":songs"), eq("json"), any());
    }

    @Test
    @DisplayName("getCachedSongs - Should return deserialized songs when found")
    void getCachedSongs_ShouldReturnSongs_WhenFound() throws Exception {
        when(valueOperations.get("user:songs:" + USER_ID + ":" + PLAYLIST_ID + ":songs")).thenReturn("json");
        when(objectMapper.readValue(eq("json"), any(TypeReference.class))).thenReturn(songList);

        List<SongDto> result = playlistCacheService.getCachedSongs(USER_ID, PLAYLIST_ID);

        assertThat(result).containsExactly(testSong);
    }

    @Test
    @DisplayName("evictSongs - Should delete songs cache")
    void evictSongs_ShouldDeleteCache() {
        playlistCacheService.evictSongs(USER_ID, PLAYLIST_ID);

        verify(redisTemplate).delete("user:songs:" + USER_ID + ":" + PLAYLIST_ID + ":songs");
    }
//
//    // ---- PLAYLIST METADATA ----
//
    @Test
    @DisplayName("cachePlaylistMetaData - Should cache playlist metadata")
    void cachePlaylistMetaData_ShouldCachePlaylistMetaData() {
        playlistCacheService.cachePlaylistMetaData(USER_ID, testPlaylist);

        verify(valueOperations).set(eq("user:playlists:" + USER_ID + ":" + testPlaylist.id() + ":metadata"), eq(testPlaylist), any());
    }

    @Test
    @DisplayName("getCachedPlaylistMetaData - Should return playlist metadata when found")
    void getCachedPlaylistMetaData_ShouldReturnPlaylist_WhenFound() {
        when(valueOperations.get("user:playlists:" + USER_ID + ":" + PLAYLIST_ID + ":metadata")).thenReturn(testPlaylist);
        when(objectMapper.convertValue(eq(testPlaylist), any(TypeReference.class))).thenReturn(testPlaylist);

        PlaylistDto result = playlistCacheService.getCachedPlaylistMetaData(USER_ID, PLAYLIST_ID);

        assertThat(result).isEqualTo(testPlaylist);
    }

    @Test
    @DisplayName("getCachedPlaylistMetaData - Should return null when not found")
    void getCachedPlaylistMetaData_ShouldReturnNull_WhenNotFound() {
        when(valueOperations.get(anyString())).thenReturn(null);

        PlaylistDto result = playlistCacheService.getCachedPlaylistMetaData(USER_ID, PLAYLIST_ID);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("evictPlaylistMetaData - Should delete playlist metadata")
    void evictPlaylistMetaData_ShouldDeleteCache() {
        playlistCacheService.evictPlaylistMetaData(USER_ID, PLAYLIST_ID);

        verify(redisTemplate).delete("user:playlists:" + USER_ID + ":" + PLAYLIST_ID + ":metadata");
    }

    // ---- ERROR HANDLING ----

    @Test
    @DisplayName("cacheSongs - Should throw exception on serialization error")
    void cacheSongs_ShouldThrowException_OnSerializationError() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("error") {});

        assertThatThrownBy(() -> playlistCacheService.cacheSongs(USER_ID, PLAYLIST_ID, songList))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to serialize songs list");
    }
}
