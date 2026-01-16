package org.kunievakateryna.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@Getter
public class EmailMessage {
    private String recipient;
    private String subject;
    private String body;
}