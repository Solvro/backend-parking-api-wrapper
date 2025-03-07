package pl.wrapper.parking.facade.dto.stats.parking.weekly;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.wrapper.parking.facade.dto.stats.parking.basis.OccupancyInfo;
import pl.wrapper.parking.facade.dto.stats.parking.basis.ParkingInfo;
import pl.wrapper.parking.facade.dto.stats.parking.basis.ParkingStats;

public record WeeklyParkingStatsResponse(
        @Schema(implementation = ParkingInfo.class) ParkingInfo parkingInfo,
        @Schema(implementation = ParkingStats.class) ParkingStats stats,
        @Schema(implementation = OccupancyInfo.class) OccupancyInfo maxOccupancyInfo,
        @Schema(implementation = OccupancyInfo.class) OccupancyInfo minOccupancyInfo) {}
