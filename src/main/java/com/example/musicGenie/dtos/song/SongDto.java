package com.example.musicGenie.dtos.song;

import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongDto{
        String id;
        String title;
        List<String> artists;
        String album;
        int popularity;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Year releaseYear;
        boolean explicit;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        YearMonth addedAt; // nullable for deezer

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof SongDto song)) return false;
                return Objects.equals(id, song.id);
        }

        @Override
        public int hashCode() {
                return Objects.hash(id);
        }
}
