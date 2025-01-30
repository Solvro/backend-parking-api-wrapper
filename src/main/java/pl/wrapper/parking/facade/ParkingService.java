package pl.wrapper.parking.facade;

import org.springframework.lang.Nullable;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

import java.util.List;

public interface ParkingService {

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

    Object getChartForToday(Integer forId);
    List<Object> getAllChartsForToday();
}
