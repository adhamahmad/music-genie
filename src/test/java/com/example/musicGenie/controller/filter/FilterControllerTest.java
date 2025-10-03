package com.example.musicGenie.controller.filter;

import java.time.Year;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.example.musicGenie.dtos.filter.PlaylistFilterRequest;
import com.example.musicGenie.dtos.filter.PlaylistFilterResponse;
import com.example.musicGenie.dtos.song.SongDto;
import com.example.musicGenie.services.filter.FilterService;
import com.example.musicGenie.services.session.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters=false) // Disable security for testing
public class FilterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FilterService filterService;

    @MockitoBean
    private SessionService sessionService;

    String accessToken = "valid-token";
    Long userId = 1L;
    MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
    }

    @Test
    void filterSongs_WithValidTokenAndSession_ReturnsOkWithFilteredSongs() throws Exception {
        // Arrange
        PlaylistFilterRequest request = PlaylistFilterRequest.builder()
                                                             .artists(Arrays.asList("Queen", "Led Zeppelin"))
                                                             .albums(Arrays.asList("A Night at the Opera", "Led Zeppelin IV"))
                                                             .popularity(80)
                                                             .releaseYear(Year.of(2005))
                                                             .explicit(false)
                                                             .provider("spotify")
                                                             .build();

        SongDto song1 = SongDto.builder()
                               .id("song1")
                               .title("Bohemian Rhapsody")
                               .artists(Arrays.asList("Queen"))
                               .album("A Night at the Opera")
                               .popularity(95)
                               .releaseYear(Year.of(2005))
                               .explicit(false)
                               .addedAt(YearMonth.of(2023, 1))
                               .build();

        SongDto song2 = SongDto.builder()
                               .id("song2")
                               .title("Stairway to Heaven")
                               .artists(Arrays.asList("Led Zeppelin"))
                               .album("Led Zeppelin IV")
                               .popularity(90)
                               .releaseYear(Year.of(2010))
                               .explicit(false)
                               .addedAt(YearMonth.of(2023, 2))
                               .build();

        List<SongDto> expectedSongs = Arrays.asList(song1, song2);

        // Mock session service
        when(sessionService.getAccessToken(session)).thenReturn(accessToken);
        when(sessionService.getUserId(session)).thenReturn(userId);

        // Mock filter service
        UUID fakeFilterId = UUID.randomUUID();
        when(filterService.filterSongs(userId, request))
                .thenReturn(new PlaylistFilterResponse(fakeFilterId, expectedSongs));

        // When & Then
        mockMvc.perform(post("/api/filter")
                       .session(session)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.filterId").value(fakeFilterId.toString()))
               .andExpect(jsonPath("$.songs").isArray())
               .andExpect(jsonPath("$.songs.length()").value(2))
               .andExpect(jsonPath("$.songs[0].id").value("song1"))
               .andExpect(jsonPath("$.songs[0].title").value("Bohemian Rhapsody"))
               .andExpect(jsonPath("$.songs[0].artists[0]").value("Queen"))
               .andExpect(jsonPath("$.songs[0].album").value("A Night at the Opera"))
               .andExpect(jsonPath("$.songs[0].popularity").value(95))
               .andExpect(jsonPath("$.songs[0].releaseYear").value("2005"))
               .andExpect(jsonPath("$.songs[0].explicit").value(false))
               .andExpect(jsonPath("$.songs[0].addedAt").value("2023-01"))
               .andExpect(jsonPath("$.songs[1].id").value("song2"))
               .andExpect(jsonPath("$.songs[1].title").value("Stairway to Heaven"))
               .andExpect(jsonPath("$.songs[1].artists[0]").value("Led Zeppelin"))
               .andExpect(jsonPath("$.songs[1].album").value("Led Zeppelin IV"))
               .andExpect(jsonPath("$.songs[1].popularity").value(90))
               .andExpect(jsonPath("$.songs[1].releaseYear").value("2010"))
               .andExpect(jsonPath("$.songs[1].explicit").value(false))
               .andExpect(jsonPath("$.songs[1].addedAt").value("2023-02"));

    }

    @Test
    void filterSongs_WithEmptyResults_ReturnsOkWithEmptyList() throws Exception {
        // Arrange
        PlaylistFilterRequest request = PlaylistFilterRequest.builder()
                                                             .artists(Arrays.asList("Unknown Artist"))
                                                             .popularity(100)
                                                             .releaseYear(Year.of(1995))
                                                             .build();

        List<SongDto> emptyResults = List.of();

        // Mock session service
        when(sessionService.getAccessToken(session)).thenReturn(accessToken);
        when(sessionService.getUserId(session)).thenReturn(userId);

        // Mock filter service
        // Mock filter service
        UUID fakeFilterId = UUID.randomUUID();
        when(filterService.filterSongs(userId, request))
                .thenReturn(new PlaylistFilterResponse(fakeFilterId, emptyResults));
        // When & Then
        mockMvc.perform(post("/api/filter")
                       .session(session)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.filterId").value(fakeFilterId.toString()))
               .andExpect(jsonPath("$.songs").isArray())
               .andExpect(jsonPath("$.songs.length()").value(0));
    }
////
    @Test
    void filterSongs_WithMinimalFilterRequest_ReturnsOkWithSongs() throws Exception {
        // Arrange
        PlaylistFilterRequest request = PlaylistFilterRequest.builder()
                                                             .artists(Arrays.asList("Ed Sheeran"))
                                                             .build();

        SongDto song = SongDto.builder()
                              .id("pop-song-1")
                              .title("Shape of You")
                              .artists(Arrays.asList("Ed Sheeran"))
                              .album("÷ (Divide)")
                              .popularity(88)
                              .releaseYear(Year.of(2017))
                              .explicit(false)
                              .addedAt(YearMonth.of(2023, 3))
                              .build();

        List<SongDto> expectedSongs = Arrays.asList(song);

        // Mock session service
        when(sessionService.getAccessToken(session)).thenReturn(accessToken);
        when(sessionService.getUserId(session)).thenReturn(userId);

        // Mock filter service
        UUID fakeFilterId = UUID.randomUUID();
        when(filterService.filterSongs(userId, request))
                .thenReturn(new PlaylistFilterResponse(fakeFilterId, expectedSongs));

        // When & Then
        mockMvc.perform(post("/api/filter")
                       .session(session)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.filterId").value(fakeFilterId.toString()))
               .andExpect(jsonPath("$.songs").isArray())
               .andExpect(jsonPath("$.songs.length()").value(1))
               .andExpect(jsonPath("$.songs[0].title").value("Shape of You"))
               .andExpect(jsonPath("$.songs[0].artists[0]").value("Ed Sheeran"));

    }
//
    @Test
    void filterSongs_WithNoAccessToken_ReturnsUnauthorized() throws Exception {
        // Arrange
        PlaylistFilterRequest request = PlaylistFilterRequest.builder()
                                                             .artists(Arrays.asList("Miles Davis"))
                                                             .albums(Arrays.asList("Kind of Blue"))
                                                             .releaseYear(Year.of(1959))
                                                             .build();

        // Mock session service to return null for access token
        when(sessionService.getAccessToken(session)).thenReturn(null);
        when(sessionService.getUserId(session)).thenReturn(userId);

        // When & Then
        mockMvc.perform(post("/api/filter")
                       .session(session)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isUnauthorized())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void filterSongs_WithComplexFilterCriteria_ReturnsOkWithMatchingSongs() throws Exception {
        // Arrange
        PlaylistFilterRequest request = PlaylistFilterRequest.builder()
                                                             .playlistIds(Arrays.asList("playlist1", "playlist2"))
                                                             .artists(Arrays.asList("Avicii"))
                                                             .albums(Arrays.asList("Stories"))
                                                             .popularity(80)
                                                             .releaseYear(Year.of(2015))
                                                             .explicit(false)
                                                             .addedAt(YearMonth.of(2023, 6))
                                                             .provider("spotify")
                                                             .build();

        SongDto electronicSong = SongDto.builder()
                                        .id("electronic-1")
                                        .title("Waiting For Love")
                                        .artists(Arrays.asList("Avicii"))
                                        .album("Stories")
                                        .popularity(85)
                                        .releaseYear(Year.of(2015))
                                        .explicit(false)
                                        .addedAt(YearMonth.of(2023, 6))
                                        .build();

        List<SongDto> expectedSongs = Arrays.asList(electronicSong);

        // Mock session service
        when(sessionService.getAccessToken(session)).thenReturn(accessToken);
        when(sessionService.getUserId(session)).thenReturn(userId);

        // Mock filter service
        // Mock filter service
        UUID fakeFilterId = UUID.randomUUID();
        when(filterService.filterSongs(userId, request))
                .thenReturn(new PlaylistFilterResponse(fakeFilterId, expectedSongs));

        // When & Then
        mockMvc.perform(post("/api/filter")
                       .session(session)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.filterId").value(fakeFilterId.toString()))
               .andExpect(jsonPath("$.songs").isArray())
               .andExpect(jsonPath("$.songs.length()").value(1))
               .andExpect(jsonPath("$.songs[0].id").value("electronic-1"))
               .andExpect(jsonPath("$.songs[0].title").value("Waiting For Love"))
               .andExpect(jsonPath("$.songs[0].artists[0]").value("Avicii"))
               .andExpect(jsonPath("$.songs[0].popularity").value(85))
               .andExpect(jsonPath("$.songs[0].releaseYear").value("2015"));

    }

    @Test
    void filterSongs_WithSpotifySpecificFilter_ReturnsOkWithSongs() throws Exception {
        // Arrange
        PlaylistFilterRequest request = PlaylistFilterRequest.builder()
                                                             .addedAt(YearMonth.of(2023, 12))
                                                             .provider("spotify")
                                                             .build();

        SongDto spotifySong = SongDto.builder()
                                     .id("spotify-recent-1")
                                     .title("Flowers")
                                     .artists(Arrays.asList("Miley Cyrus"))
                                     .album("Endless Summer Vacation")
                                     .popularity(92)
                                     .releaseYear(Year.of(2023))
                                     .explicit(false)
                                     .addedAt(YearMonth.of(2023, 12))
                                     .build();

        List<SongDto> expectedSongs = Arrays.asList(spotifySong);

        // Mock session service
        when(sessionService.getAccessToken(session)).thenReturn(accessToken);
        when(sessionService.getUserId(session)).thenReturn(userId);

        // Mock filter service
        // Mock filter service
        UUID fakeFilterId = UUID.randomUUID();
        when(filterService.filterSongs(userId, request))
                .thenReturn(new PlaylistFilterResponse(fakeFilterId, expectedSongs));

        // When & Then
        mockMvc.perform(post("/api/filter")
                       .session(session)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.filterId").value(fakeFilterId.toString()))
               .andExpect(jsonPath("$.songs").isArray())
               .andExpect(jsonPath("$.songs.length()").value(1))
               .andExpect(jsonPath("$.songs[0].id").value("spotify-recent-1"))
               .andExpect(jsonPath("$.songs[0].title").value("Flowers"))
               .andExpect(jsonPath("$.songs[0].artists[0]").value("Miley Cyrus"))
               .andExpect(jsonPath("$.songs[0].addedAt").value("2023-12"));

    }
//
    @Test
    void filterSongs_WithInvalidRequestBody_ReturnsBadRequest() throws Exception {
        // Arrange - invalid JSON
        String invalidJson = "{ invalid json }";

        // Mock session service
        when(sessionService.getAccessToken(session)).thenReturn(accessToken);
        when(sessionService.getUserId(session)).thenReturn(userId);

        // When & Then
        mockMvc.perform(post("/api/filter")
                       .session(session)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(invalidJson))
               .andExpect(status().isBadRequest());
    }

    @Test
    void filterSongs_WithNullSession_ReturnsUnauthorized() throws Exception {
        // Arrange
        PlaylistFilterRequest request = PlaylistFilterRequest.builder()
                                                             .artists(Arrays.asList("Johnny Cash"))
                                                             .build();

        // Mock session service to return null for access token (simulating no session)
        when(sessionService.getAccessToken(null)).thenReturn(null);
        when(sessionService.getUserId(null)).thenReturn(null);

        // When & Then - not passing session
        mockMvc.perform(post("/api/filter")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isUnauthorized());
    }
}