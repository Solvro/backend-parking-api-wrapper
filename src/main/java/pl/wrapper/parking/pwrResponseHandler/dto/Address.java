package pl.wrapper.parking.pwrResponseHandler.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record Address(
        @Schema(example = "Example 201, 11-041 Wrocław") String streetAddress,
        @Schema(example = "21.37") float geoLatitude,
        @Schema(example = "-4.20") float geoLongitude) {}
