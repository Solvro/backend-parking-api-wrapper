package pl.wrapper.parking.facade.domain;

import static java.time.temporal.TemporalAdjusters.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.facade.dto.NominatimLocation;
import pl.wrapper.parking.facade.dto.stats.*;
import pl.wrapper.parking.infrastructure.error.ParkingError;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.infrastructure.inMemory.ParkingDataRepository;
import pl.wrapper.parking.infrastructure.inMemory.dto.AvailabilityData;
import pl.wrapper.parking.infrastructure.inMemory.dto.ParkingData;
import pl.wrapper.parking.infrastructure.nominatim.client.NominatimClient;
import pl.wrapper.parking.infrastructure.util.DateTimeUtils;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

@Service
@Slf4j
public record ParkingServiceImpl(
        PwrApiServerCaller pwrApiServerCaller,
        NominatimClient nominatimClient,
        ParkingDataRepository dataRepository,
        @Value("${pwr-api.data-fetch.minutes}") Integer minuteInterval)
        implements ParkingService {

    @Override
    public List<ParkingStatsResponse> getParkingStats(
            @Nullable List<Integer> parkingIds, @Nullable DayOfWeek dayOfWeek, LocalTime time) {
        Collection<ParkingData> dataList = getParkingDataList(parkingIds);

        LocalDateTime roundedDateTime = (dayOfWeek == null)
                ? DateTimeUtils.roundToNearestInterval(LocalDateTime.now().with(time), minuteInterval)
                : DateTimeUtils.roundToNearestInterval(
                        LocalDateTime.now().with(nextOrSame(dayOfWeek)).with(time), minuteInterval);

        LocalTime roundedTime = roundedDateTime.toLocalTime();
        DayOfWeek roundedDay = roundedDateTime.getDayOfWeek();

        List<ParkingStatsResponse> result = new ArrayList<>();

        if (dayOfWeek != null) {
            for (ParkingData data : dataList) {
                AvailabilityData availabilityData = data.freeSpotsHistory()
                        .getOrDefault(roundedDay, Map.of())
                        .get(roundedTime);
                int parkingId = data.parkingId();
                if (availabilityData != null) {
                    double availability = availabilityData.averageAvailability();
                    ParkingStats stats = ParkingStats.builder()
                            .parkingId(parkingId)
                            .averageAvailability(round(availability))
                            .averageFreeSpots((int) (availability * data.totalSpots()))
                            .build();
                    result.add(new ParkingStatsResponse(stats));
                } else {
                    result.add(new ParkingStatsResponse(ParkingStats.builder()
                            .parkingId(parkingId)
                            .build()));
                }
            }
        } else {
            for (ParkingData data : dataList) {
                int parkingId = data.parkingId();
                data.freeSpotsHistory().values().stream()
                        .map(dailyHistory -> dailyHistory.get(roundedTime))
                        .filter(Objects::nonNull)
                        .mapToDouble(AvailabilityData::averageAvailability)
                        .average()
                        .ifPresentOrElse(availability ->
                                result.add(new ParkingStatsResponse(ParkingStats.builder()
                                        .parkingId(parkingId)
                                        .averageAvailability(round(availability))
                                        .averageFreeSpots((int) (availability * data.totalSpots()))
                                        .build())),
                                () -> result.add(new ParkingStatsResponse(ParkingStats.builder()
                                        .parkingId(parkingId)
                                        .build()))
                        );
            }
        }

        return result;
    }

    @Override
    public List<DailyParkingStatsResponse> getDailyParkingStats(@Nullable List<Integer> parkingIds, DayOfWeek dayOfWeek) {
        return processParkingDataDaily(dayOfWeek, getParkingDataList(parkingIds));
    }

    @Override
    public List<WeeklyParkingStatsResponse> getWeeklyParkingStats(@Nullable List<Integer> parkingIds) {
        return processParkingDataWeekly(getParkingDataList(parkingIds));
    }

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
                .map(location -> {
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

    private Stream<ParkingResponse> getStreamOfFilteredFetchedParkingLots(
            Predicate<ParkingResponse> filteringPredicate) {
        return pwrApiServerCaller.fetchData().stream().filter(filteringPredicate);
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
        log.info("Parking found");
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

    private Collection<ParkingData> getParkingDataList(List<Integer> parkingIds) {
        if(parkingIds == null || parkingIds.isEmpty()) return dataRepository.values();
        Set<Integer> ids = new HashSet<>(dataRepository.fetchAllKeys());
        ids.retainAll(parkingIds);
        return ids.isEmpty() ? dataRepository.values() : ids.stream().map(dataRepository::get).toList();
    }

    private static List<DailyParkingStatsResponse> processParkingDataDaily(DayOfWeek dayOfWeek, Collection<ParkingData> dataList) {
        List<DailyParkingStatsResponse> result = new ArrayList<>();
        for (ParkingData data : dataList) {
            List<Double> availabilities = new ArrayList<>();
            Map<LocalTime, Double> availabilityMap = new HashMap<>();
            data.freeSpotsHistory().getOrDefault(dayOfWeek, Map.of()).forEach((key, value) -> {
                double availability = value.averageAvailability();
                availabilities.add(availability);
                availabilityMap.put(key, availability);
            });
            double averageAvailability = availabilities.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            ParkingStats stats = ParkingStats.builder()
                    .parkingId(data.parkingId())
                    .averageAvailability(round(averageAvailability))
                    .averageFreeSpots((int) (averageAvailability * data.totalSpots()))
                    .build();
            LocalTime maxOccupancyAt = findMaxOccupancy(availabilityMap);
            LocalTime minOccupancyAt = findMinOccupancy(availabilityMap);
            result.add(new DailyParkingStatsResponse(stats, maxOccupancyAt, minOccupancyAt));
        }
        return result;
    }

    private static List<WeeklyParkingStatsResponse> processParkingDataWeekly(Collection<ParkingData> dataList) {
        List<WeeklyParkingStatsResponse> result = new ArrayList<>();
        for (ParkingData data : dataList) {
            List<Double> availabilities = new ArrayList<>();
            Map<OccupancyInfo, Double> availabilityMap = new HashMap<>();
            data.freeSpotsHistory()
                    .forEach((day, dailyHistory) -> dailyHistory.forEach((key, value) -> {
                        double availability = value.averageAvailability();
                        availabilities.add(availability);
                        availabilityMap.put(new OccupancyInfo(day, key), availability);
                    }));
            double averageAvailability = availabilities.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            ParkingStats stats = ParkingStats.builder()
                    .parkingId(data.parkingId())
                    .averageAvailability(round(averageAvailability))
                    .averageFreeSpots((int) (averageAvailability * data.totalSpots()))
                    .build();
            OccupancyInfo maxOccupancyInfo = findMaxOccupancy(availabilityMap);
            OccupancyInfo minOccupancyInfo = findMinOccupancy(availabilityMap);
            result.add(new WeeklyParkingStatsResponse(stats, maxOccupancyInfo, minOccupancyInfo));
        }
        return result;
    }

    private static <T> T findMaxOccupancy(Map<T, Double> availabilityMap) {
        return availabilityMap.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static <T> T findMinOccupancy(Map<T, Double> availabilityMap) {
        return availabilityMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static double round(double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }
}
