package pl.wrapper.parking.facade;

import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ParkingService {
    Mono<List<ParkingResponse>> getAllParkings();

    Mono<ParkingResponse> getClosestParking(String address);
}
