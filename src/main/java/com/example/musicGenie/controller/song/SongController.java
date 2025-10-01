package com.example.musicGenie.controller.song;

import java.util.List;

import com.example.musicGenie.dtos.playlist.PlaylistDto;
import com.example.musicGenie.dtos.song.SongDto;
import com.example.musicGenie.services.playlist.PlaylistService;
import com.example.musicGenie.services.session.SessionService;
import com.example.musicGenie.services.song.SongService;
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
    public ResponseEntity<List<SongDto>> getPlaylistSongs(@PathVariable String provider, @RequestParam String playlistId, HttpSession session) {
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
