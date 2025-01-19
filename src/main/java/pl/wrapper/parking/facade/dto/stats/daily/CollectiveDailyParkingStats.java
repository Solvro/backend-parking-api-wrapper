package pl.wrapper.parking.facade.dto.stats.daily;

import java.time.LocalTime;
import java.util.Map;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingInfo;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingStats;

public record CollectiveDailyParkingStats(ParkingInfo parkingInfo, Map<LocalTime, ParkingStats> statsMap) {}
