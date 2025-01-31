package pl.wrapper.parking.facade.dto.historicData;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record HistoricDayData(@Schema(type = "string", format = "date", example = "2025-08-23") LocalDate atDate, @ArraySchema(schema = @Schema(implementation = TimestampEntry.class)) List<TimestampEntry> data) {
}
