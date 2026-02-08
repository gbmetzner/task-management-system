package com.gbm.taskapi.dto.response;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(int status, String message, OffsetDateTime timestamp, Map<String, String> errors) {
    public ErrorResponse(int status, String message, OffsetDateTime timestamp) {
        this(status, message, timestamp, null);
    }
}
