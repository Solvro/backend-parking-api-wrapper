package pl.wrapper.parking.facade.dto.historicData;

import io.swagger.v3.oas.annotations.media.Schema;

public record TimestampEntry(
        @Schema(type = "string", format = "time", example = "12:45") String timestamp, short freeSpots) {}
