package org.kunievakateryna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Getter
@Builder
@Jacksonized
@Schema(description = "Author of anime")
public class AuthorDto {
    @Schema(description = "Author id", example = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6", required = true)
    private UUID id;

    @Schema(description = "Author name", example = "The Walt Disney Studios", required = true)
    private String name;
}
