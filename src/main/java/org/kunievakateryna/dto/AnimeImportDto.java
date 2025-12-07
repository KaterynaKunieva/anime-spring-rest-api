package org.kunievakateryna.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AnimeImportDto {
    private String title;
    private String author;
    private Double score;
    private Integer year;
}
