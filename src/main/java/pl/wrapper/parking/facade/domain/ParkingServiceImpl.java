package pl.wrapper.parking.facade.domain;

import org.springframework.stereotype.Service;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.facade.client.NominatimClient;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
record ParkingServiceImpl(PwrApiServerCaller pwrApiServerCaller, NominatimClient nominatimClient) implements ParkingService {
    //1. endpointy:
    // - endpoint dla każdego parametru, zwraca jeden, kilka, lub nic
    // - research, closest address
    // - most free spots, everything that has => free spots, eveything has any free spots
    // -* error handling, try to use as few exceptions as possible (think of a good way to do it)
    //   dokumentacja + swagger do enpointa/endpointów i testy jednostkowe + test integracyjny dla waszego enpointa/enpoiontów

    @Override
    public Mono<List<ParkingResponse>> getAllParkings() {
        return pwrApiServerCaller.fetchData();
    }

    @Override
    public Mono<ParkingResponse> getClosestParking(String address) {
        return Mono.empty();
    }
}
