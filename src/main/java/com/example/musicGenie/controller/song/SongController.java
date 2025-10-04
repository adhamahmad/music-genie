package com.example.musicGenie.controller.song;

import java.util.List;

import com.example.musicGenie.dtos.playlist.PlaylistDto;
import com.example.musicGenie.dtos.song.SongDto;
import com.example.musicGenie.services.playlist.PlaylistService;
import com.example.musicGenie.services.session.SessionService;
import com.example.musicGenie.services.song.SongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {
    private final SongService songService;
    private final SessionService sessionService;

    @GetMapping("/{provider}")
    @Operation(
            summary = "Get songs from a playlist",
            description = """
                Retrieves all songs for the given playlist ID from the specified music provider.
                - The `playlistId` must be obtained from the **Get Playlists** API.
                - Currently, only the `spotify` provider is supported.
                Requires the user to be authenticated with the music provider.
                """
    )
    public ResponseEntity<List<SongDto>> getPlaylistSongs(
            @Parameter(
                    description = "Music provider. Currently only 'spotify' is supported.",
                    schema = @Schema(allowableValues = {"spotify"})
            )
            @PathVariable String provider,

            @Parameter(
                    description = "The ID of the playlist (must be obtained from the Get Playlists API)."
            )
            @RequestParam String playlistId, HttpSession session) {
        String accessToken = sessionService.getAccessToken(session);
        Long userId = sessionService.getUserId(session);

        if (accessToken == null) {
            // no token in session → return 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(List.of()); // empty list in body
        }
        List<SongDto> songs = songService.getPlaylistSongs(provider, playlistId, userId);
        return ResponseEntity.ok(songs); // 200 OK with body


    }
}
