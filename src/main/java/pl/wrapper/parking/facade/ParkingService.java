package pl.wrapper.parking.facade;

import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

public interface ParkingService {

    Result<ParkingResponse> getByName(String name);

    Result<ParkingResponse> getById(Integer id);

    Result<ParkingResponse> getBySymbol(String symbol);
}
