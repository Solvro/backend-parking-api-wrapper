package pl.wrapper.parking.pwrResponseHandler;

import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PwrApiServerCaller {
    Mono<List<ParkingResponse>> fetchData();
}
