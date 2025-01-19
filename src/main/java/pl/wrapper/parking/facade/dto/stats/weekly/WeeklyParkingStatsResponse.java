package pl.wrapper.parking.facade.dto.stats.weekly;

import pl.wrapper.parking.facade.dto.stats.basis.OccupancyInfo;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingInfo;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingStats;

public record WeeklyParkingStatsResponse(
        ParkingInfo parkingInfo, ParkingStats stats, OccupancyInfo maxOccupancyInfo, OccupancyInfo minOccupancyInfo) {}
