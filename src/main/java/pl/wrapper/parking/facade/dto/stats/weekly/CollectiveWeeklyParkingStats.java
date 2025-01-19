package pl.wrapper.parking.facade.dto.stats.weekly;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingInfo;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingStats;

public record CollectiveWeeklyParkingStats(
        ParkingInfo parkingInfo, Map<DayOfWeek, Map<LocalTime, ParkingStats>> statsMap) {}
