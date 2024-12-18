package pl.wrapper.parking.facade.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.facade.client.NominatimClient;
import pl.wrapper.parking.facade.dto.NominatimLocation;
import pl.wrapper.parking.infrastructure.error.ParkingError;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

import java.util.*;
import java.util.function.Predicate;

@Service
@Slf4j
record ParkingServiceImpl(PwrApiServerCaller pwrApiServerCaller, NominatimClient nominatimClient) implements ParkingService {

    @Override
    public List<ParkingResponse> getAllWithFreeSpots(Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(null, null, null, opened);
        predicate = predicate.and(parking -> parking.freeSpots() > 0);

        return pwrApiServerCaller.fetchData().stream().filter(predicate).toList();
    }

    @Override
    public Result<ParkingResponse> getWithTheMostFreeSpots(Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(null, null, null, opened);
        List<ParkingResponse> parkings = pwrApiServerCaller.fetchData().stream().filter(predicate).toList();

        return parkings.stream()
                .max(Comparator.comparingInt(ParkingResponse::freeSpots))
                .map(this::handleFoundParking).orElse(Result.failure(new ParkingError.NoFreeParkingSpotsAvailable()));
    }

    @Override
    public Result<ParkingResponse> getClosestParking(String address) {
        Optional<NominatimLocation> geoLocation = nominatimClient.search(address, "json").next().blockOptional();
        return geoLocation.map(location -> {
                    log.info("Geocoded address for coordinates: {} {}", location.latitude(), location.longitude());
                    return findClosestParking(location, pwrApiServerCaller.fetchData())
                            .map(Result::success)
                            .orElse(Result.failure(new ParkingError.ParkingNotFoundByAddress(address)));
                })
                .orElseGet(() -> {
                    log.warn("No geocoding results for address: {}", address);
                    return Result.failure(new ParkingError.ParkingNotFoundByAddress(address));
                });
    }

    @Override
    public Result<ParkingResponse> getByName(String name,Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(null, null, name, opened);

        return findParking(predicate)
                .map(this::handleFoundParking).orElse(Result.failure(new ParkingError.ParkingNotFoundByName(name)));
    }

    @Override
    public Result<ParkingResponse> getById(Integer id,Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(null, id, null, opened);

        return findParking(predicate)
                .map(this::handleFoundParking).orElse(Result.failure(new ParkingError.ParkingNotFoundById(id)));
    }

    @Override
    public Result<ParkingResponse> getBySymbol(String symbol,Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(symbol, null, null, opened);

        return findParking(predicate)
                .map(this::handleFoundParking).orElse(Result.failure(new ParkingError.ParkingNotFoundBySymbol(symbol)));

    }

    @Override
    public List<ParkingResponse> getByParams(String symbol, Integer id, String name, Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(symbol, id, name, opened);

        return pwrApiServerCaller.fetchData().stream()
                .filter(predicate)
                .toList();
    }

    private Optional<ParkingResponse> findParking(Predicate<ParkingResponse> predicate){
        return pwrApiServerCaller.fetchData().stream()
                .filter(predicate)
                .findFirst();
    }

    private Optional<ParkingResponse> findClosestParking(NominatimLocation location, List<ParkingResponse> parkingLots) {
        double lat = location.latitude();
        double lon = location.longitude();

        return parkingLots.stream()
                .min(Comparator.comparingDouble(parking -> haversineDistance(
                        lat, lon,
                        parking.address().geoLatitude(),
                        parking.address().geoLongitude()))
                );
    }

    private static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371;

        double havLat = (1 - Math.cos(Math.toRadians(lat2 - lat1))) / 2;
        double havLon = (1 - Math.cos(Math.toRadians(lon2 - lon1))) / 2;
        double haversine = havLat + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * havLon;

        return 2 * EARTH_RADIUS * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private Result<ParkingResponse> handleFoundParking(ParkingResponse found){
        log.info("Parking found");
        return Result.success(found);
    }

    private Predicate<ParkingResponse> generatePredicateForParams(String symbol,Integer id ,String name, Boolean isOpened){
        Predicate<ParkingResponse> predicate = parking -> true;
        if (symbol != null)
            predicate = predicate.and(parking -> symbol.toLowerCase().contains(parking.symbol().toLowerCase()));
        if (id != null)
            predicate = predicate.and(parking -> Objects.equals(id, parking.parkingId()));
        if (name != null)
            predicate = predicate.and(parking -> name.toLowerCase().contains(parking.name().toLowerCase()));
        if (isOpened != null)
            predicate = predicate.and(parking -> Objects.equals(isOpened, parking.isOpened()));

        return predicate;
    }
}
