package pl.wrapper.parking.facade.dto.stats;

import pl.wrapper.parking.facade.dto.stats.basis.ParkingInfo;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingStats;

public record ParkingStatsResponse(ParkingInfo parkingInfo, ParkingStats stats) {}
