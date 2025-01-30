package pl.wrapper.parking.facade;

import org.springframework.lang.Nullable;
import pl.wrapper.parking.facade.dto.stats.ParkingStatsResponse;
import pl.wrapper.parking.facade.dto.stats.daily.CollectiveDailyParkingStats;
import pl.wrapper.parking.facade.dto.stats.daily.DailyParkingStatsResponse;
import pl.wrapper.parking.facade.dto.stats.weekly.CollectiveWeeklyParkingStats;
import pl.wrapper.parking.facade.dto.stats.weekly.WeeklyParkingStatsResponse;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface ParkingStatsService {
    List<ParkingStatsResponse> getParkingStats(
            @Nullable List<Integer> parkingIds, @Nullable DayOfWeek dayOfWeek, LocalTime time);

    List<DailyParkingStatsResponse> getDailyParkingStats(@Nullable List<Integer> parkingIds, DayOfWeek dayOfWeek);

    List<WeeklyParkingStatsResponse> getWeeklyParkingStats(@Nullable List<Integer> parkingIds);

    List<CollectiveDailyParkingStats> getCollectiveDailyParkingStats(
            @Nullable List<Integer> parkingIds, DayOfWeek dayOfWeek);

    List<CollectiveWeeklyParkingStats> getCollectiveWeeklyParkingStats(@Nullable List<Integer> parkingIds);
}
