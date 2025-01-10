package pl.wrapper.parking.facade;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.springframework.lang.Nullable;
import pl.wrapper.parking.facade.dto.stats.DailyParkingStatsResponse;
import pl.wrapper.parking.facade.dto.stats.ParkingStatsResponse;
import pl.wrapper.parking.facade.dto.stats.WeeklyParkingStatsResponse;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

public interface ParkingService {
    Result<ParkingStatsResponse> getParkingStats(
            @Nullable Integer parkingId, @Nullable DayOfWeek dayOfWeek, LocalTime time);

    Result<DailyParkingStatsResponse> getDailyParkingStats(@Nullable Integer parkingId, DayOfWeek dayOfWeek);

    Result<WeeklyParkingStatsResponse> getWeeklyParkingStats(@Nullable Integer parkingId);

    List<ParkingResponse> getAllWithFreeSpots(@Nullable Boolean opened);

    Result<ParkingResponse> getWithTheMostFreeSpots(@Nullable Boolean opened);

    Result<ParkingResponse> getClosestParking(String address);

    Result<ParkingResponse> getByName(String name, @Nullable Boolean opened);

    Result<ParkingResponse> getById(Integer id, @Nullable Boolean opened);

    Result<ParkingResponse> getBySymbol(String symbol, @Nullable Boolean opened);

    List<ParkingResponse> getByParams(
            @Nullable String symbol,
            @Nullable Integer id,
            @Nullable String name,
            @Nullable Boolean opened,
            @Nullable Boolean hasFreeSpots);
}
