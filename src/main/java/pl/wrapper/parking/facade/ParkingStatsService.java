package pl.wrapper.parking.facade;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.springframework.lang.Nullable;
import pl.wrapper.parking.facade.dto.stats.parking.ParkingStatsResponse;
import pl.wrapper.parking.facade.dto.stats.parking.daily.CollectiveDailyParkingStats;
import pl.wrapper.parking.facade.dto.stats.parking.daily.DailyParkingStatsResponse;
import pl.wrapper.parking.facade.dto.stats.parking.weekly.CollectiveWeeklyParkingStats;
import pl.wrapper.parking.facade.dto.stats.parking.weekly.WeeklyParkingStatsResponse;

public interface ParkingStatsService {
    List<ParkingStatsResponse> getParkingStats(
            @Nullable List<Integer> parkingIds, @Nullable DayOfWeek dayOfWeek, LocalTime time);

    List<DailyParkingStatsResponse> getDailyParkingStats(@Nullable List<Integer> parkingIds, DayOfWeek dayOfWeek);

    List<WeeklyParkingStatsResponse> getWeeklyParkingStats(@Nullable List<Integer> parkingIds);

    List<CollectiveDailyParkingStats> getCollectiveDailyParkingStats(
            @Nullable List<Integer> parkingIds, DayOfWeek dayOfWeek);

    List<CollectiveWeeklyParkingStats> getCollectiveWeeklyParkingStats(@Nullable List<Integer> parkingIds);
}
