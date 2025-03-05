package pl.wrapper.parking.facade.dto.stats.parking.basis;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record OccupancyInfo(
        @Schema(examples = "WEDNESDAY") DayOfWeek dayOfWeek,
        @Schema(type = "string", format = "time", example = "15:30:00") LocalTime time) {}
