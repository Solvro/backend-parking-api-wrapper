package pl.wrapper.parking.facade.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.facade.client.NominatimClient;
import pl.wrapper.parking.facade.dto.NominatimLocation;
import pl.wrapper.parking.facade.exception.AddressNotFoundException;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
record ParkingServiceImpl(PwrApiServerCaller pwrApiServerCaller, NominatimClient nominatimClient) implements ParkingService {

    @Override
    public Result<List<ParkingResponse>> getAllParkings() {
        return Result.success(pwrApiServerCaller.fetchData());
    }

    @Override
    public Result<ParkingResponse> getClosestParking(String address) {
        Optional<NominatimLocation> geoLocation = nominatimClient.search(address, "json").next().blockOptional();
        return Result.success(
                geoLocation.map(location -> {
                    log.info("Geocoded address for coordinates: {} {}", location.latitude(), location.longitude());
                    return findClosestParking(location, getAllParkings().getData());
                })
                .orElseThrow(() -> {
                    log.warn("No geocoding results for address: {}", address);
                    return new AddressNotFoundException(address);
                })
        );
    }

    private ParkingResponse findClosestParking(NominatimLocation location, List<ParkingResponse> parkingLots) {
        double lat = location.latitude();
        double lon = location.longitude();

        return parkingLots.stream()
                .min(Comparator.comparingDouble(parking -> haversineDistance(
                        lat, lon,
                        parking.address().geoLatitude(),
                        parking.address().geoLongitude())))
                .orElseThrow(() -> new NoSuchElementException("No parkings available"));
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371;

        double havLat = (1 - Math.cos(Math.toRadians(lat2 - lat1))) / 2;
        double havLon = (1 - Math.cos(Math.toRadians(lon2 - lon1))) / 2;
        double haversine = havLat + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * havLon;

        return 2 * EARTH_RADIUS * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }
}
