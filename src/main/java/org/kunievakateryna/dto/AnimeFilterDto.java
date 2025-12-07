package org.kunievakateryna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AnimeFilterDto {
    @Schema(description = "Filter by author ID", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID authorId;

    @Min(value=1900, message = "Release year must be between 1900 and 2100")
    @Max(value=2100, message = "Release year must be between 1900 and 2100")
    @Schema(description = "Filter by release year", example = "2020", minimum = "1900", maximum = "2100")
    private Integer releaseYear;
}