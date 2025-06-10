package com.messmanagement.common.exception;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Don't include null fields in the JSON output
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error; // Short error description e.g., "Bad Request"
    private String message; // More detailed human-readable message
    private String path;
    private List<ErrorDetail> details; // For validation errors

    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String message, String path, List<ErrorDetail> details) {
        this(timestamp, status, error, message, path);
        this.details = details;
    }
}