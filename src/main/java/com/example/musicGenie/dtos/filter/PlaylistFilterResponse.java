package com.example.musicGenie.dtos.filter;

import java.util.List;
import java.util.UUID;

import com.example.musicGenie.dtos.song.SongDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistFilterResponse {
    private UUID filterId;
    private List<SongDto> songs;
}


