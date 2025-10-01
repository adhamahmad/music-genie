package com.example.musicGenie.playlist;

import java.util.List;

import com.example.musicGenie.dtos.playlist.PlaylistDto;
import com.example.musicGenie.dtos.song.SongDto;

public interface PlaylistProvider {
    List<PlaylistDto> fetchUserPlaylists(String accessToken);
    PlaylistDto fetchUserPlaylist(String accessToken, String playlistId);
    PlaylistDto createPlaylist(String accessToken, String playlistName);
    void addSongsToPlaylist(String accessToken, String playlistId, List<SongDto> songs);
}
