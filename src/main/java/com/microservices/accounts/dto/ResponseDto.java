package com.microservices.accounts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(name = "Response", description = "Schema holding successful response information")
@Data
@AllArgsConstructor
public class ResponseDto {

    @Schema(description = "HTTP status code of the response")
    private String statusCode;

    @Schema(description = "HTTP status message of the response")
    private String statusMsg;
}
