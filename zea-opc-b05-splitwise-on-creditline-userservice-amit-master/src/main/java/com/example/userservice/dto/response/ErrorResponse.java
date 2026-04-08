package com.example.userservice.dto.response;

import com.example.userservice.exceptions.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private final long timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
                .timestamp(System.currentTimeMillis())
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .message(message)
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(System.currentTimeMillis())
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }
}
