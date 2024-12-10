package pl.wrapper.parking.facade;

import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

import java.util.List;

public interface ParkingService {
    Result<List<ParkingResponse>> getAllParkings();

    Result<ParkingResponse> getClosestParking(String address);
}
