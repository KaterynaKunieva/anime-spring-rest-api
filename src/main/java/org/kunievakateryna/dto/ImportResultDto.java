package org.kunievakateryna.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ImportResultDto {
    private int successfulImportCount;
    private int failedImportCount;
}