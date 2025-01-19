package pl.wrapper.parking.facade.dto.stats.daily;

import java.time.LocalTime;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingInfo;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingStats;

public record DailyParkingStatsResponse(
        ParkingInfo parkingInfo, ParkingStats stats, LocalTime maxOccupancyAt, LocalTime minOccupancyAt) {}
