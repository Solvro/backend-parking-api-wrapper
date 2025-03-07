package pl.wrapper.parking.facade.dto.stats.parking;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.wrapper.parking.facade.dto.stats.parking.basis.ParkingInfo;
import pl.wrapper.parking.facade.dto.stats.parking.basis.ParkingStats;

public record ParkingStatsResponse(
        @Schema(implementation = ParkingInfo.class) ParkingInfo parkingInfo,
        @Schema(implementation = ParkingStats.class) ParkingStats stats) {}
