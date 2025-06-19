package com.nna.assignment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErrorResponse {
    private String message;
    private String error;
    private int status;
    private String path;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private List<ValidationError> validationErrors;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message, String error, int status, String path) {
        this();
        this.message = message;
        this.error = error;
        this.status = status;
        this.path = path;
    }
}