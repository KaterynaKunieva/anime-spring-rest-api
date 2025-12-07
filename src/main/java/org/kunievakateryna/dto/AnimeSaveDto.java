package org.kunievakateryna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import java.util.UUID;

@Getter
@Setter
@Builder
@Jacksonized
public class AnimeSaveDto {
    @NotBlank(message = "Title cannot be empty")
    @Size(min = 1, max = 255)
    @Schema(description = "Title of anime", example = "Naruto")
    private String title;

    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "10.0", inclusive = true)
    @Schema(description = "Anime score (0.0-10.0). Optional", example = "9.5", nullable = true)
    private Double score;

    @Min(1900)
    @Max(2100)
    @NotNull(message = "Release year is required")
    @Schema(description = "Anime release year", example = "2005")
    private Integer releaseYear;

    @NotNull(message = "Author ID cannot be null")
    @Schema(description = "UUID of author", example = "1c8fcad0-28d4-4d12-b2a4-8ae6a8646f51")
    private UUID authorId;
}