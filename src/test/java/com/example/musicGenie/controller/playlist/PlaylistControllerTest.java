package com.example.musicGenie.controller.playlist;

import java.time.Year;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

import com.example.musicGenie.dtos.playlist.CreatePlaylistRequest;
import com.example.musicGenie.dtos.playlist.PlaylistDto;
import com.example.musicGenie.services.playlist.PlaylistService;
import com.example.musicGenie.services.session.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters=false) // Disable security for testing
public class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlaylistService playlistService;

    @MockitoBean
    private SessionService sessionService;

    String provider = "spotify";
    String accessToken = "valid-token";
    Long userId = 1L;
    MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
    }

    @Test
    void getUserPlaylists_WithValidTokenAndSession_ReturnsOkWithPlaylists() throws Exception {
        PlaylistDto playlist1 = PlaylistDto.builder()
                                           .id("playlist1")
                                           .name("My Favorites")
                                           .owner("user123")
                                           .tracksCount(25)
                                           .imageUrl("https://example.com/image1.jpg")
                                           .build();

        PlaylistDto playlist2 = PlaylistDto.builder()
                                           .id("playlist2")
                                           .name("Workout Mix")
                                           .owner("user123")
                                           .tracksCount(15)
                                           .imageUrl("https://example.com/image2.jpg")
                                           .build();

        List<PlaylistDto> expectedPlaylists = Arrays.asList(playlist1, playlist2);

        // Mock session service
        when(sessionService.getAccessToken(session)).thenReturn(accessToken);
        when(sessionService.getUserId(session)).thenReturn(userId);

        // Mock playlist service
        when(playlistService.getUserPlaylists(provider, accessToken, userId, false))
                .thenReturn(expectedPlaylists);

        // When & Then
        mockMvc.perform(get("/api/playlists/{provider}", provider)
                       .session(session)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(2))
               .andExpect(jsonPath("$[0].id").value("playlist1"))
               .andExpect(jsonPath("$[0].name").value("My Favorites"))
               .andExpect(jsonPath("$[0].tracksCount").value(25))
               .andExpect(jsonPath("$[0].owner").value("user123"))
               .andExpect(jsonPath("$[0].imageUrl").value("https://example.com/image1.jpg"))
               .andExpect(jsonPath("$[1].id").value("playlist2"))
               .andExpect(jsonPath("$[1].name").value("Workout Mix"))
               .andExpect(jsonPath("$[1].tracksCount").value(15))
               .andExpect(jsonPath("$[1].owner").value("user123"))
               .andExpect(jsonPath("$[1].imageUrl").value("https://example.com/image2.jpg"));
    }

    @Test
    void getUserPlaylists_WithForceRefreshTrue_ReturnsOkWithPlaylists() throws Exception {
        PlaylistDto playlist = PlaylistDto.builder()
                                          .id("playlist1")
                                          .name("Refreshed Playlist")
                                          .owner("user123")
                                          .tracksCount(10)
                                          .imageUrl("https://example.com/image.jpg")
                                          .build();

        List<PlaylistDto> expectedPlaylists = Arrays.asList(playlist);

        // Mock session service
        when(sessionService.getAccessToken(session)).thenReturn(accessToken);
        when(sessionService.getUserId(session)).thenReturn(userId);

        // Mock playlist service with forceRefresh = true
        when(playlistService.getUserPlaylists(provider, accessToken, userId, true))
                .thenReturn(expectedPlaylists);

        // When & Then
        mockMvc.perform(get("/api/playlists/{provider}", provider)
                       .param("forceRefresh", "true")
                       .session(session)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].name").value("Refreshed Playlist"));
    }

    @Test
    void getUserPlaylists_WithNoAccessToken_ReturnsUnauthorized() throws Exception {
        // Mock session service to return null for access token
        when(sessionService.getAccessToken(session)).thenReturn(null);
        when(sessionService.getUserId(session)).thenReturn(userId);

        // When & Then
        mockMvc.perform(get("/api/playlists/{provider}", provider)
                       .session(session)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnauthorized())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createPlaylist_WithValidTokenAndSession_ReturnsOkWithNewPlaylist() throws Exception {
        CreatePlaylistRequest request = CreatePlaylistRequest.builder()
                                                             .filterId("filter123")
                                                             .name("New Playlist")
                                                             .build();

        PlaylistDto createdPlaylist = PlaylistDto.builder()
                                                 .id("new-playlist-123")
                                                 .name("New Playlist")
                                                 .owner("user123")
                                                 .tracksCount(0)
                                                 .imageUrl("https://example.com/default.jpg")
                                                 .build();

        // Mock session service
        when(sessionService.getAccessToken(session)).thenReturn(accessToken);
        when(sessionService.getUserId(session)).thenReturn(userId);

        // Mock playlist service
        when(playlistService.createPlaylistFromFilter(provider, accessToken, userId, request))
                .thenReturn(createdPlaylist);

        // When & Then
        mockMvc.perform(post("/api/playlists/{provider}", provider)
                       .session(session)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.id").value("new-playlist-123"))
               .andExpect(jsonPath("$.name").value("New Playlist"))
               .andExpect(jsonPath("$.owner").value("user123"))
               .andExpect(jsonPath("$.tracksCount").value(0))
               .andExpect(jsonPath("$.imageUrl").value("https://example.com/default.jpg"));
    }

    @Test
    void createPlaylist_WithNoAccessToken_ReturnsUnauthorized() throws Exception {
        CreatePlaylistRequest request = CreatePlaylistRequest.builder()
                                                             .filterId("filter123")
                                                             .name("New Playlist")
                                                             .build();

        // Mock session service to return null for access token
        when(sessionService.getAccessToken(session)).thenReturn(null);
        when(sessionService.getUserId(session)).thenReturn(userId);

        // When & Then
        mockMvc.perform(post("/api/playlists/{provider}", provider)
                       .session(session)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void createPlaylist_WithNoUserId_ReturnsUnauthorized() throws Exception {
        CreatePlaylistRequest request = CreatePlaylistRequest.builder()
                                                             .filterId("filter123")
                                                             .name("New Playlist")
                                                             .build();

        // Mock session service to return null for user ID
        when(sessionService.getAccessToken(session)).thenReturn(accessToken);
        when(sessionService.getUserId(session)).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/playlists/{provider}", provider)
                       .session(session)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void createPlaylist_WithNullAccessTokenAndUserId_ReturnsUnauthorized() throws Exception {
        CreatePlaylistRequest request = CreatePlaylistRequest.builder()
                                                             .filterId("filter123")
                                                             .name("New Playlist")
                                                             .build();

        // Mock session service to return null for both
        when(sessionService.getAccessToken(session)).thenReturn(null);
        when(sessionService.getUserId(session)).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/playlists/{provider}", provider)
                       .session(session)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isUnauthorized());
    }
}