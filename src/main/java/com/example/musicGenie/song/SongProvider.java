package com.example.musicGenie.song;

import java.util.List;

import com.example.musicGenie.dtos.playlist.PlaylistDto;
import com.example.musicGenie.dtos.song.SongDto;

public interface SongProvider {
    List<SongDto> fetchPlaylistSongs(String playlistId);
}
