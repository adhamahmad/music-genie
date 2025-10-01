package com.example.musicGenie.dtos.filter;

import java.time.Year;
import java.time.YearMonth;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.AssertTrue;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown properties during deserialization
public class PlaylistFilterRequest {
    List<String> playlistIds;
    private List<String> artists;
    private List<String> albums;
    private Integer popularity;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Year releaseYear;
    private Boolean explicit;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private YearMonth addedAt; // Only for Spotify

    private String provider; // "spotify" or "deezer" for now

    @AssertTrue(message = "At least one filter must be provided")
    public boolean isValid() {
        return (artists != null && !artists.isEmpty())
                || (albums != null && !albums.isEmpty())
                || popularity != null
                || releaseYear != null
                || explicit != null
                || (addedAt != null && provider != null && provider.equals("spotify"));
    }
}
