package pl.wrapper.parking.facade.dto.stats.weekly;

import java.util.Map;
import pl.wrapper.parking.facade.dto.stats.basis.OccupancyInfo;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingInfo;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingStats;

public record CollectiveWeeklyParkingStats(ParkingInfo parkingInfo, Map<OccupancyInfo, ParkingStats> statsMap) {}
