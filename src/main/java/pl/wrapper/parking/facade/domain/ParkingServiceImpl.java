package pl.wrapper.parking.facade.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.facade.client.NominatimClient;
import pl.wrapper.parking.facade.dto.NominatimLocation;
import pl.wrapper.parking.facade.exception.AddressNotFoundException;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
record ParkingServiceImpl(PwrApiServerCaller pwrApiServerCaller, NominatimClient nominatimClient) implements ParkingService {

    @Override
    public Mono<List<ParkingResponse>> getAllParkings() {
        return pwrApiServerCaller.fetchData();
    }

    @Override
    public Mono<ParkingResponse> getClosestParking(String address) {
        Mono<NominatimLocation> geoLocation = nominatimClient.search(address, "json").next();
        return geoLocation
                .flatMap(location -> {
                    log.info("Geocoded address for coordinates: {} {}", location.getLatitude(), location.getLongitude());
                    return findClosestParking(location, getAllParkings());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No geocoding results for address: {}", address);
                    return Mono.error(new AddressNotFoundException(address));
                }));
    }

    private Mono<ParkingResponse> findClosestParking(NominatimLocation location, Mono<List<ParkingResponse>> parkingLots) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        return parkingLots.flatMap(parkings -> Mono.just(parkings.stream()
                .min(Comparator.comparingDouble(parking -> haversineDistance(
                        lat, lon,
                        parking.address().geoLatitude(),
                        parking.address().geoLongitude())))
                .orElseThrow(() -> new NoSuchElementException("No parkings available")))
        );
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371;

        double havLat = (1 - Math.cos(Math.toRadians(lat2 - lat1))) / 2;
        double havLon = (1 - Math.cos(Math.toRadians(lon2 - lon1))) / 2;
        double haversine = havLat + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * havLon;

        return 2 * EARTH_RADIUS * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }
}
