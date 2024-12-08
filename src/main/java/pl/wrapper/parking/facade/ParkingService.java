package pl.wrapper.parking.facade;

import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

import java.util.List;

public interface ParkingService {

    Result<ParkingResponse> getByName(String name, Boolean opened);

    Result<ParkingResponse> getById(Integer id, Boolean opened);

    Result<ParkingResponse> getBySymbol(String symbol, Boolean opened);

    List<ParkingResponse> getByParams(String symbol, Integer id, String name, Boolean opened);
}
