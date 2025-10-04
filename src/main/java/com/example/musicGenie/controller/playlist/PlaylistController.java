package com.example.musicGenie.controller.playlist;

import java.util.List;

import com.example.musicGenie.dtos.playlist.CreatePlaylistRequest;
import com.example.musicGenie.dtos.playlist.PlaylistDto;
import com.example.musicGenie.services.playlist.PlaylistService;
import com.example.musicGenie.services.session.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {
    private final PlaylistService playlistService;
    private final SessionService sessionService;

    @GetMapping("/{provider}")
    @Operation(
            summary = "Get user playlists",
            description = """
                Returns all playlists for the current user from the given music provider.
                Currently supported provider: **spotify**.
                If `forceRefresh=true`, playlists will be fetched directly from the provider, bypassing the cache.
                """
    )
    public ResponseEntity<List<PlaylistDto>> getUserPlaylists(
            @Parameter(
                    description = "Music provider. Currently only 'spotify' is supported.",
                    schema = @Schema(allowableValues = {"spotify"})
            )
            @PathVariable String provider,
            HttpSession session, @RequestParam(required = false) Boolean forceRefresh
    ) {
        String accessToken = sessionService.getAccessToken(session);
        Long userId = sessionService.getUserId(session);
        if (accessToken == null) {
            // no token in session → return 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(List.of()); // empty list in body
        }
        List<PlaylistDto> playlists = playlistService.getUserPlaylists(provider, accessToken,userId, forceRefresh != null && forceRefresh);
        return ResponseEntity.ok(playlists); // 200 OK with body
    }

    @PostMapping("/{provider}")
    @Operation(
            summary = "Create a new playlist",
            description = """
                Creates a new playlist for the authenticated user on the given music provider.
                
                - The request must include a **filterId** (obtained from the Filter API) and a **playlist name**.
                - Currently, only the `spotify` provider is supported.
                - Requires authentication with the music provider.
                """
    )
    public ResponseEntity<PlaylistDto> createPlaylist(
            @Parameter(
                    description = "Music provider. Currently only 'spotify' is supported.",
                    schema = @Schema(allowableValues = {"spotify"})
            )
            @PathVariable String provider,
            @RequestBody CreatePlaylistRequest request,
            HttpSession session
    ) {
        String accessToken = sessionService.getAccessToken(session);
        Long userId = sessionService.getUserId(session);

        if (accessToken == null || userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PlaylistDto newPlaylist = playlistService.createPlaylistFromFilter(
                provider,
                accessToken,
                userId,
                request
        );

        return ResponseEntity.ok(newPlaylist);
    }


}


