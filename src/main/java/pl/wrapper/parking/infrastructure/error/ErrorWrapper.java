package pl.wrapper.parking.infrastructure.error;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

public record ErrorWrapper(@Schema(example = "An error has occured") String errorMessage,
                           @Schema(example = "INTERNAL_SERVER_ERROR") HttpStatus expectedStatus,
                           @Schema(example = "/v1/name") String Uri,
                           @Schema(example = "INTERNAL_SERVER_ERROR") HttpStatus occuredstatus) {}
