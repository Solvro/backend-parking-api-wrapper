package pl.wrapper.parking.facade.domain.main;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.facade.dto.main.NominatimLocation;
import pl.wrapper.parking.infrastructure.error.ParkingError;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.infrastructure.nominatim.client.NominatimClient;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

@Service
@Slf4j
record ParkingServiceImpl(PwrApiServerCaller pwrApiServerCaller, NominatimClient nominatimClient)
        implements ParkingService {

    @Override
    public List<ParkingResponse> getAllWithFreeSpots(@Nullable Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(null, null, null, opened, true);
        return getStreamOfFilteredFetchedParkingLots(predicate).toList();
    }

    @Override
    public Result<ParkingResponse> getWithTheMostFreeSpots(@Nullable Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(null, null, null, opened, null);
        return getStreamOfFilteredFetchedParkingLots(predicate)
                .max(Comparator.comparingInt(ParkingResponse::freeSpots))
                .map(this::handleFoundParking)
                .orElse(Result.failure(new ParkingError.NoFreeParkingSpotsAvailable()));
    }

    @Override
    public Result<ParkingResponse> getClosestParking(String address) {
        Optional<NominatimLocation> geoLocation =
                nominatimClient.search(address, "json").next().blockOptional();
        return geoLocation
                .map(location -> findClosestParking(location, pwrApiServerCaller.fetchParkingData())
                        .map(Result::success)
                        .orElse(Result.failure(new ParkingError.ParkingNotFoundByAddress(address))))
                .orElseGet(() -> {
                    log.info("No geocoding results for address: {}", address);
                    return Result.failure(new ParkingError.ParkingNotFoundByAddress(address));
                });
    }

    @Override
    public Result<ParkingResponse> getByName(String name, @Nullable Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(null, null, name, opened, null);
        return findParking(predicate)
                .map(this::handleFoundParking)
                .orElse(Result.failure(new ParkingError.ParkingNotFoundByName(name)));
    }

    @Override
    public Result<ParkingResponse> getById(Integer id, @Nullable Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(null, id, null, opened, null);
        return findParking(predicate)
                .map(this::handleFoundParking)
                .orElse(Result.failure(new ParkingError.ParkingNotFoundById(id)));
    }

    @Override
    public Result<ParkingResponse> getBySymbol(String symbol, @Nullable Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(symbol, null, null, opened, null);
        return findParking(predicate)
                .map(this::handleFoundParking)
                .orElse(Result.failure(new ParkingError.ParkingNotFoundBySymbol(symbol)));
    }

    @Override
    public List<ParkingResponse> getByParams(
            @Nullable String symbol,
            @Nullable Integer id,
            @Nullable String name,
            @Nullable Boolean opened,
            @Nullable Boolean hasFreeSpots) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(symbol, id, name, opened, hasFreeSpots);
        return getStreamOfFilteredFetchedParkingLots(predicate).toList();
    }

    @Override
    public Object getChartForToday(Integer forId) {
        return pwrApiServerCaller.getAllCharsForToday().get(forId - 1);
    }

    @Override
    public List<Object> getAllChartsForToday() {
        return pwrApiServerCaller.getAllCharsForToday();
    }

    private Stream<ParkingResponse> getStreamOfFilteredFetchedParkingLots(
            Predicate<ParkingResponse> filteringPredicate) {
        return pwrApiServerCaller.fetchParkingData().stream().filter(filteringPredicate);
    }

    private Optional<ParkingResponse> findParking(Predicate<ParkingResponse> predicate) {
        return getStreamOfFilteredFetchedParkingLots(predicate).findFirst();
    }

    private Optional<ParkingResponse> findClosestParking(
            NominatimLocation location, List<ParkingResponse> parkingLots) {
        double lat = location.latitude();
        double lon = location.longitude();

        return parkingLots.stream()
                .min(Comparator.comparingDouble(parking -> haversineDistance(
                        lat,
                        lon,
                        parking.address().geoLatitude(),
                        parking.address().geoLongitude())));
    }

    private static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371;

        double havLat = (1 - Math.cos(Math.toRadians(lat2 - lat1))) / 2;
        double havLon = (1 - Math.cos(Math.toRadians(lon2 - lon1))) / 2;
        double haversine = havLat + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * havLon;

        return 2 * EARTH_RADIUS * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private Result<ParkingResponse> handleFoundParking(ParkingResponse found) {
        return Result.success(found);
    }

    private Predicate<ParkingResponse> generatePredicateForParams(
            String symbol, Integer id, String name, Boolean isOpened, Boolean hasFreeSpots) {
        Predicate<ParkingResponse> predicate = parking -> true;
        if (symbol != null)
            predicate = predicate.and(
                    parking -> symbol.toLowerCase().contains(parking.symbol().toLowerCase()));
        if (id != null) predicate = predicate.and(parking -> Objects.equals(id, parking.parkingId()));
        if (name != null)
            predicate = predicate.and(
                    parking -> name.toLowerCase().contains(parking.name().toLowerCase()));
        if (isOpened != null) predicate = predicate.and(parking -> Objects.equals(isOpened, parking.isOpened()));
        if (hasFreeSpots != null) predicate = predicate.and(parking -> hasFreeSpots == (parking.freeSpots() > 0));

        return predicate;
    }
}
