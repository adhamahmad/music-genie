package com.example.musicGenie.services.playlist;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.musicGenie.dtos.playlist.PlaylistDto;
import com.example.musicGenie.dtos.song.SongDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistCacheService {
    private final ObjectMapper objectMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PLAYLIST_CACHE_KEY = "user:playlists:";
    private static final String SONGS_CACHE_KEY = "user:songs:";
    private static final String FILTERED_CACHE_KEY = "user:filtered:";

    // ---- FILTERED SONGS ----
    private String filteredSongsKey(Long userId, String filterId) {
        return FILTERED_CACHE_KEY + userId + ":" + filterId + ":songs";
    }

    public UUID cacheFilteredSongs(Long userId, List<SongDto> songs) {
        UUID uuid = UUID.randomUUID();
        String filterId = uuid.toString();
        String key = filteredSongsKey(userId, filterId);
        try {
            // Convert list to JSON string
            String json = objectMapper.writeValueAsString(songs);
            redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(30));
        } catch (
                JsonProcessingException e) {
            System.out.println("Error serializing filtered songs list: " + e.getMessage());
            throw new RuntimeException("Failed to serialize filtered songs list", e);
        }
        return uuid;
    }

    public List<SongDto> getCachedFilteredSongs(Long userId, String filterId) {
        String key = filteredSongsKey(userId, filterId);
        String json = (String) redisTemplate.opsForValue().get(key);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<SongDto>>() {});
        } catch (
                JsonProcessingException e) {
            System.out.println("Error serializing filtered songs : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void evictFilteredSongs(Long userId, String filterId) {
        String key = filteredSongsKey(userId, filterId);
        redisTemplate.delete(key);
    }


    // ---- SONGS ----
    private String playlistSongsKey(Long userId, String playlistId) {
        return SONGS_CACHE_KEY + userId + ":" + playlistId + ":songs";
    }

    private String playlistMetaKey(Long userId, String playlistId) {
        return PLAYLIST_CACHE_KEY + userId + ":" + playlistId + ":metadata";
    }

    public void cacheSongs(Long userId, String playlistId, List<SongDto> songs) {
        String key = playlistSongsKey(userId, playlistId);
        try {
            // Convert list to JSON string
            String json = objectMapper.writeValueAsString(songs);
            redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(30));
        } catch (
                JsonProcessingException e) {
                System.out.println("Error serializing songs list: " + e.getMessage());
            throw new RuntimeException("Failed to serialize songs list", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<SongDto> getCachedSongs(Long userId, String playlistId) {
        String key = playlistSongsKey(userId, playlistId);
        String json = (String) redisTemplate.opsForValue().get(key);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<SongDto>>() {});
        } catch (
                JsonProcessingException e) {
            System.out.println("Error serializing songs : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void evictSongs(Long userId, String playlistId) {
        String key = playlistSongsKey(userId, playlistId);
        redisTemplate.delete(key);
    }

    // ---- PLAYLIST METADATA ----

    public void cachePlaylistsMetaData(Long userId, List<PlaylistDto> playlists) {
        for(PlaylistDto playlist: playlists){
            String key = playlistMetaKey(userId, playlist.id());
            redisTemplate.opsForValue().set(key, playlist, Duration.ofMinutes(30));
        }
    }

    public void cachePlaylistMetaData(Long userId, PlaylistDto playlist) {
            String key = playlistMetaKey(userId, playlist.id());
            redisTemplate.opsForValue().set(key, playlist, Duration.ofMinutes(30));
    }


    public List<PlaylistDto> getCachedPlaylistsMetaData(Long userId) {
        String pattern = PLAYLIST_CACHE_KEY + userId + ":*"; // scan all user playlists
        List<PlaylistDto> results = new ArrayList<>();

        // use scan instead of keys (keys is blocking in prod)
        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                                                  .getConnection()
                                                  .scan(ScanOptions.scanOptions().match(pattern).build())) {
            while (cursor.hasNext()) {
                String key = new String(cursor.next());
                Object raw = redisTemplate.opsForValue().get(key);
                if (raw != null) {
                    results.add(objectMapper.convertValue(raw, new TypeReference<PlaylistDto>() {}));
                }
            }
        }

        return results.isEmpty() ? null : results;
    }

    public void evictPlaylistMetaData(Long userId, String playlistId){
        String key = playlistMetaKey(userId, playlistId);
        redisTemplate.delete(key);
    }

    public PlaylistDto getCachedPlaylistMetaData(Long userId, String playlistId) {
        String key = playlistMetaKey(userId, playlistId);
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw == null) return null;

        return objectMapper.convertValue(raw, new TypeReference<PlaylistDto>() {});
    }
}
