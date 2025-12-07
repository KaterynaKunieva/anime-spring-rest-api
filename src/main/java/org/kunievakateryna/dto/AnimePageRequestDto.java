package org.kunievakateryna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AnimePageRequestDto extends AnimeFilterDto {
    @Min(0)
    @Max(100000)
    @Schema(description = "Page index", example = "0", minimum = "0", maximum = "100000")
    private Integer page = 0;

    @Min(1)
    @Max(1000)
    @Schema(description = "Items per page", example = "20", minimum = "1", maximum = "1000")
    private Integer size = 20;
}
