package com.microservices.accounts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Schema(name = "Error Response", description = "Schema holding error response information")
@Data
@AllArgsConstructor
public class ErrorResponseDto {

    @Schema(description = "API path that raised the error", example = "/api/fetch")
    private String apiPath;

    @Schema(description = "HTTP error status code", example = "404")
    private HttpStatus errorCode;

    @Schema(description = "Human-readable error message", example = "Customer not found with the given mobile number")
    private String errorMessage;

    @Schema(description = "Timestamp when the error occurred", example = "2026-02-26T10:15:30")
    private LocalDateTime errorTime;
}
