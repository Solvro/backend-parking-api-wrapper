package pl.wrapper.parking.facade.dto.stats.daily;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingInfo;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingStats;

import java.time.LocalTime;
import java.util.Map;

public record CollectiveDailyParkingStats(
        @Schema(implementation = ParkingInfo.class) ParkingInfo parkingInfo, Map<LocalTime, ParkingStats> statsMap) {}
