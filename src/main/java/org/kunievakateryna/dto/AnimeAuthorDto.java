package org.kunievakateryna.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Getter
@Builder
@Jacksonized
@Schema(description = "Anime with author data")
public class AnimeAuthorDto {
    @Schema(description = "Anime id", example = "acde070d-8c4c-4f0d-9d8a-162843c10333", required = true)
    private UUID id;

    @Schema(description = "Anime title", example = "Naruto", required=true)
    private String title;

    @Schema(description = "Anime score", example = "9.5")
    private Double score;

    @Schema(description = "Anime release year", example = "2005")
    private Integer releaseYear;

    @Schema(description = "Anime author id", example = "The Walt Disney Studios", required=true)
    private AuthorDto author;
}
