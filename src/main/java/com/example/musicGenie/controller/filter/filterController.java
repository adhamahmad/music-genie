package com.example.musicGenie.controller.filter;

import java.util.List;

import com.example.musicGenie.dtos.filter.PlaylistFilterRequest;
import com.example.musicGenie.dtos.filter.PlaylistFilterResponse;
import com.example.musicGenie.dtos.song.SongDto;
import com.example.musicGenie.services.filter.FilterService;
import com.example.musicGenie.services.playlist.PlaylistCacheService;
import com.example.musicGenie.services.playlist.PlaylistService;
import com.example.musicGenie.services.session.SessionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/filter")
@RequiredArgsConstructor
public class filterController {
    private final FilterService filterService;
    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<PlaylistFilterResponse> filterSongs(HttpSession session, @RequestBody @Valid PlaylistFilterRequest request) {
        String accessToken = sessionService.getAccessToken(session);
        Long userId = sessionService.getUserId(session);
        if (accessToken == null) {
            // no token in session → return 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(new PlaylistFilterResponse()); // empty list in body
        }
        PlaylistFilterResponse filteredSongsResponse = filterService.filterSongs(userId, request);
        return ResponseEntity.ok(filteredSongsResponse);
    }
}
