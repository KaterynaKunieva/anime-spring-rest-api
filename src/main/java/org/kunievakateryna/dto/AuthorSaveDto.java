package org.kunievakateryna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Builder
@Jacksonized
@Schema(description = "Data for adding or updating an anime author")
public class AuthorSaveDto {
    @NotBlank(message = "Author name cannot be empty")
    @Size(min = 1, max = 100, message = "Name must be 1-100 chars")
    @Schema(description = "Author name", example = "The Walt Disney Studios", required = true)
    private String name;
}
