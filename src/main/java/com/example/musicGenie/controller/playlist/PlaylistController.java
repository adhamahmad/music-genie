package com.example.musicGenie.controller.playlist;

import java.util.List;

import com.example.musicGenie.dtos.playlist.CreatePlaylistRequest;
import com.example.musicGenie.dtos.playlist.PlaylistDto;
import com.example.musicGenie.services.playlist.PlaylistService;
import com.example.musicGenie.services.session.SessionService;
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
    public ResponseEntity<List<PlaylistDto>> getUserPlaylists(@PathVariable String provider,HttpSession session, @RequestParam(required = false) Boolean forceRefresh) {
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
    public ResponseEntity<PlaylistDto> createPlaylist(
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


