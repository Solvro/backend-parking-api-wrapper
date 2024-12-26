package pl.wrapper.parking.infrastructure.error;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

public record ErrorWrapper(
        @Schema(example = "An error has occurred") String errorMessage,
        @Schema(example = "INTERNAL_SERVER_ERROR") HttpStatus expectedStatus,
        @Schema(example = "/v1/parkings/name") String uri,
        @Schema(example = "INTERNAL_SERVER_ERROR") HttpStatus occurredStatus) {}
