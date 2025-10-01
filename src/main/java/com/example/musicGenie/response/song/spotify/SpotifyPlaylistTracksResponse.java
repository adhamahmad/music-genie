package com.example.musicGenie.response.song.spotify;

import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.musicGenie.dtos.song.SongDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyPlaylistTracksResponse {
    private List<SpotifyPlaylistTrackItem> items;

    public List<SongDto> toSongDtos() {
        if (items == null) return Collections.emptyList();

        return items.stream()
                    .map(item -> {
                        var track = item.getTrack();
                        if (track == null) return null;

                        List<String> artists = track.getArtists() != null
                                ? track.getArtists().stream()
                                       .map(SpotifyArtist::getName)
                                       .collect(Collectors.toList())
                                : Collections.emptyList();

                        // Extract albumName and releaseYear
                        String albumName = track.getAlbum() != null ? track.getAlbum().getName() : null;
                        Year releaseYear = null;
                        if (track.getAlbum() != null && track.getAlbum().getReleaseDate() != null) {
                            try {
                                String releaseDate = track.getAlbum().getReleaseDate();
                                if (releaseDate.length() >= 4) {
                                    releaseYear = Year.of(Integer.parseInt(releaseDate.substring(0, 4)));
                                }
                            } catch (Exception ignored) {}
                        }

                        // Extract addedAt from item
                        YearMonth addedAt = null;
                        if (item.getAddedAt() != null) {
                            try {
                                YearMonth ym = YearMonth.parse(
                                        item.getAddedAt().substring(0, 7),
                                        DateTimeFormatter.ofPattern("yyyy-MM")
                                );
                                addedAt = ym;
                            } catch (Exception ignored) {}
                        }

                        return SongDto.builder()
                                      .id(track.getId())
                                      .title(track.getName())
                                      .artists(artists)
                                      .album(albumName)
                                      .popularity(track.getPopularity())
                                      .releaseYear(releaseYear)
                                      .explicit(track.isExplicit())
                                      .addedAt(addedAt)
                                      .build();
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
    }
}
