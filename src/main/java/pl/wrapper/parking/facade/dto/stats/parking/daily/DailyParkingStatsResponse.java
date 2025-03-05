package pl.wrapper.parking.facade.dto.stats.parking.daily;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import pl.wrapper.parking.facade.dto.stats.parking.basis.ParkingInfo;
import pl.wrapper.parking.facade.dto.stats.parking.basis.ParkingStats;

public record DailyParkingStatsResponse(
        @Schema(implementation = ParkingInfo.class) ParkingInfo parkingInfo,
        @Schema(implementation = ParkingStats.class) ParkingStats stats,
        @Schema(type = "string", format = "time", example = "15:30:00") LocalTime maxOccupancyAt,
        @Schema(type = "string", format = "time", example = "02:30:00") LocalTime minOccupancyAt) {}
