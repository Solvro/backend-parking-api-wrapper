package pl.wrapper.parking.facade.dto.stats.parking.daily;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.Map;
import pl.wrapper.parking.facade.dto.stats.parking.basis.ParkingInfo;
import pl.wrapper.parking.facade.dto.stats.parking.basis.ParkingStats;

public record CollectiveDailyParkingStats(
        @Schema(implementation = ParkingInfo.class) ParkingInfo parkingInfo, Map<LocalTime, ParkingStats> statsMap) {}
