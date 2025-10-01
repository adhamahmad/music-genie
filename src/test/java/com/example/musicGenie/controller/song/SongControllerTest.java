package com.example.musicGenie.controller.song;

import java.time.Year;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

import com.example.musicGenie.config.security.SecurityConfig;
import com.example.musicGenie.dtos.song.SongDto;
import com.example.musicGenie.services.session.SessionService;
import com.example.musicGenie.services.song.SongService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc(addFilters=false) // Disable security for testing
public class SongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SongService songService;

    @MockitoBean
    private SessionService sessionService;

    String provider = "spotify";
    String playlistId = "playlist123";
    String accessToken = "valid-token";
    Long userId = 1L;
    MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
    }

    @Test
    void getPlaylistSongs_WithValidTokenAndSession_ReturnsOkWithSongs() throws Exception {
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
        List<SongDto> expectedSongs = Arrays.asList(
                song1, song2
        );

        // Mock session service
        when(sessionService.getAccessToken(session)).thenReturn(accessToken);
        when(sessionService.getUserId(session)).thenReturn(userId);

        // Mock song service
        when(songService.getPlaylistSongs(provider, playlistId, userId))
                .thenReturn(expectedSongs);

// When & Then
        mockMvc.perform(get("/api/songs/{provider}", provider)
                       .param("playlistId", playlistId)
                       .session(session)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(2))
               .andExpect(jsonPath("$[0].title").value("Song One"))
               .andExpect(jsonPath("$[0].artists[0]").value("Artist A"))
               .andExpect(jsonPath("$[0].album").value("Album A"))
               .andExpect(jsonPath("$[0].popularity").value(85))
               .andExpect(jsonPath("$[0].releaseYear").value(2021))
               .andExpect(jsonPath("$[0].explicit").value(false))
               .andExpect(jsonPath("$[0].addedAt").value("2023-05"))
               .andExpect(jsonPath("$[1].title").value("Song Two"))
               .andExpect(jsonPath("$[1].artists[0]").value("Artist B"))
               .andExpect(jsonPath("$[1].artists[1]").value("Artist C"))
               .andExpect(jsonPath("$[1].album").value("Album B"))
               .andExpect(jsonPath("$[1].popularity").value(70))
               .andExpect(jsonPath("$[1].releaseYear").value(2020))
               .andExpect(jsonPath("$[1].explicit").value(true))
               .andExpect(jsonPath("$[1].addedAt").value("2023-06"));

    }
}
