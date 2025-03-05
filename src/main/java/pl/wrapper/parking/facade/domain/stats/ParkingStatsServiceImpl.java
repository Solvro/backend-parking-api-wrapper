package pl.wrapper.parking.facade.domain.stats;

import static java.time.temporal.TemporalAdjusters.nextOrSame;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import pl.wrapper.parking.facade.ParkingStatsService;
import pl.wrapper.parking.facade.dto.stats.ParkingStatsResponse;
import pl.wrapper.parking.facade.dto.stats.basis.OccupancyInfo;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingInfo;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingStats;
import pl.wrapper.parking.facade.dto.stats.daily.CollectiveDailyParkingStats;
import pl.wrapper.parking.facade.dto.stats.daily.DailyParkingStatsResponse;
import pl.wrapper.parking.facade.dto.stats.weekly.CollectiveWeeklyParkingStats;
import pl.wrapper.parking.facade.dto.stats.weekly.WeeklyParkingStatsResponse;
import pl.wrapper.parking.infrastructure.inMemory.ParkingDataRepository;
import pl.wrapper.parking.infrastructure.inMemory.dto.parking.AvailabilityData;
import pl.wrapper.parking.infrastructure.inMemory.dto.parking.ParkingData;
import pl.wrapper.parking.infrastructure.util.DateTimeUtils;

@Service
record ParkingStatsServiceImpl(
        ParkingDataRepository dataRepository, @Value("${pwr-api.data-fetch.minutes}") Integer minuteInterval)
        implements ParkingStatsService {

    @Override
    public List<ParkingStatsResponse> getParkingStats(
            @Nullable List<Integer> parkingIds, @Nullable DayOfWeek dayOfWeek, LocalTime time) {
        Collection<ParkingData> dataList = getParkingDataList(parkingIds);

        if (dayOfWeek != null) {
            LocalDateTime roundedDateTime = DateTimeUtils.roundToNearestInterval(
                    LocalDateTime.now().with(nextOrSame(dayOfWeek)).with(time), minuteInterval);
            return calculateStatsByDay(dataList, roundedDateTime.getDayOfWeek(), roundedDateTime.toLocalTime());
        } else {
            LocalDateTime roundedDateTime =
                    DateTimeUtils.roundToNearestInterval(LocalDateTime.now().with(time), minuteInterval);
            return calculateStatsByTime(dataList, roundedDateTime.toLocalTime());
        }
    }

    @Override
    public List<DailyParkingStatsResponse> getDailyParkingStats(
            @Nullable List<Integer> parkingIds, DayOfWeek dayOfWeek) {
        return processParkingDataDaily(dayOfWeek, getParkingDataList(parkingIds));
    }

    @Override
    public List<WeeklyParkingStatsResponse> getWeeklyParkingStats(@Nullable List<Integer> parkingIds) {
        return processParkingDataWeekly(getParkingDataList(parkingIds));
    }

    @Override
    public List<CollectiveDailyParkingStats> getCollectiveDailyParkingStats(
            @Nullable List<Integer> parkingIds, DayOfWeek dayOfWeek) {
        return processCollectiveParkingDataDaily(dayOfWeek, getParkingDataList(parkingIds));
    }

    @Override
    public List<CollectiveWeeklyParkingStats> getCollectiveWeeklyParkingStats(@Nullable List<Integer> parkingIds) {
        return processCollectiveParkingDataWeekly(getParkingDataList(parkingIds));
    }

    private Collection<ParkingData> getParkingDataList(List<Integer> parkingIds) {
        if (parkingIds == null || parkingIds.isEmpty()) return dataRepository.values();
        Set<Integer> ids = new HashSet<>(dataRepository.fetchAllKeys());
        ids.retainAll(parkingIds);
        return ids.isEmpty()
                ? dataRepository.values()
                : ids.stream().map(dataRepository::get).toList();
    }

    private static List<ParkingStatsResponse> calculateStatsByDay(
            Collection<ParkingData> dataList, DayOfWeek roundedDay, LocalTime roundedTime) {
        List<ParkingStatsResponse> result = new ArrayList<>();
        for (ParkingData data : dataList) {
            AvailabilityData availabilityData =
                    data.freeSpotsHistory().getOrDefault(roundedDay, Map.of()).get(roundedTime);
            double availability = Objects.requireNonNullElse(availabilityData, new AvailabilityData(0, 0.0))
                    .averageAvailability();
            ParkingStatsResponse response = calculateParkingStats(data, List.of(availability));
            result.add(new ParkingStatsResponse(response.parkingInfo(), response.stats()));
        }
        return result;
    }

    private static List<ParkingStatsResponse> calculateStatsByTime(
            Collection<ParkingData> dataList, LocalTime roundedTime) {
        List<ParkingStatsResponse> result = new ArrayList<>();
        for (ParkingData data : dataList) {
            data.freeSpotsHistory().values().stream()
                    .map(dailyHistory -> dailyHistory.get(roundedTime))
                    .filter(Objects::nonNull)
                    .mapToDouble(AvailabilityData::averageAvailability)
                    .average()
                    .ifPresentOrElse(
                            availability -> result.add(calculateParkingStats(data, List.of(availability))),
                            () -> result.add(calculateParkingStats(data, List.of())));
        }
        return result;
    }

    private static List<DailyParkingStatsResponse> processParkingDataDaily(
            DayOfWeek dayOfWeek, Collection<ParkingData> dataList) {
        List<DailyParkingStatsResponse> result = new ArrayList<>();
        for (ParkingData data : dataList) {
            List<Double> availabilities = new ArrayList<>();
            LocalTime maxOccupancyAt = null;
            LocalTime minOccupancyAt = null;
            double maxAvailability = Double.NEGATIVE_INFINITY;
            double minAvailability = Double.POSITIVE_INFINITY;

            for (Map.Entry<LocalTime, AvailabilityData> entry :
                    data.freeSpotsHistory().getOrDefault(dayOfWeek, Map.of()).entrySet()) {
                LocalTime time = entry.getKey();
                double availability = entry.getValue().averageAvailability();
                availabilities.add(availability);

                if (availability > maxAvailability) {
                    maxAvailability = availability;
                    minOccupancyAt = time;
                }
                if (availability < minAvailability) {
                    minAvailability = availability;
                    maxOccupancyAt = time;
                }
            }

            ParkingStatsResponse response = calculateParkingStats(data, availabilities);
            result.add(new DailyParkingStatsResponse(
                    response.parkingInfo(), response.stats(), maxOccupancyAt, minOccupancyAt));
        }
        return result;
    }

    private static List<WeeklyParkingStatsResponse> processParkingDataWeekly(Collection<ParkingData> dataList) {
        List<WeeklyParkingStatsResponse> result = new ArrayList<>();
        for (ParkingData data : dataList) {
            List<Double> availabilities = new ArrayList<>();
            OccupancyInfo maxOccupancyInfo = null;
            OccupancyInfo minOccupancyInfo = null;
            double maxAvailability = Double.NEGATIVE_INFINITY;
            double minAvailability = Double.POSITIVE_INFINITY;

            for (Map.Entry<DayOfWeek, Map<LocalTime, AvailabilityData>> dailyEntry :
                    data.freeSpotsHistory().entrySet()) {
                DayOfWeek day = dailyEntry.getKey();
                for (Map.Entry<LocalTime, AvailabilityData> timeEntry :
                        dailyEntry.getValue().entrySet()) {
                    LocalTime time = timeEntry.getKey();
                    double availability = timeEntry.getValue().averageAvailability();
                    availabilities.add(availability);

                    OccupancyInfo occupancyInfo = new OccupancyInfo(day, time);
                    if (availability > maxAvailability) {
                        maxAvailability = availability;
                        minOccupancyInfo = occupancyInfo;
                    }
                    if (availability < minAvailability) {
                        minAvailability = availability;
                        maxOccupancyInfo = occupancyInfo;
                    }
                }
            }

            ParkingStatsResponse response = calculateParkingStats(data, availabilities);
            result.add(new WeeklyParkingStatsResponse(
                    response.parkingInfo(), response.stats(), maxOccupancyInfo, minOccupancyInfo));
        }
        return result;
    }

    private static ParkingStatsResponse calculateParkingStats(ParkingData data, List<Double> availabilities) {
        double averageAvailability = availabilities.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        ParkingInfo info = ParkingInfo.builder()
                .parkingId(data.parkingId())
                .totalSpots(data.totalSpots())
                .build();
        ParkingStats stats = ParkingStats.builder()
                .averageAvailability(round(averageAvailability))
                .averageFreeSpots((int) (averageAvailability * data.totalSpots()))
                .build();
        return new ParkingStatsResponse(info, stats);
    }

    private static List<CollectiveDailyParkingStats> processCollectiveParkingDataDaily(
            DayOfWeek dayOfWeek, Collection<ParkingData> dataList) {
        List<CollectiveDailyParkingStats> result = new ArrayList<>();
        for (ParkingData data : dataList) {
            ParkingInfo info = new ParkingInfo(data.parkingId(), data.totalSpots());
            Map<LocalTime, ParkingStats> statsMap = new TreeMap<>();
            data.freeSpotsHistory().getOrDefault(dayOfWeek, Map.of()).forEach((key, value) -> {
                double availability = value.averageAvailability();
                ParkingStats stats = ParkingStats.builder()
                        .averageAvailability(round(availability))
                        .averageFreeSpots((int) (availability * data.totalSpots()))
                        .build();
                statsMap.put(key, stats);
            });
            result.add(new CollectiveDailyParkingStats(info, statsMap));
        }
        return result;
    }

    private static List<CollectiveWeeklyParkingStats> processCollectiveParkingDataWeekly(
            Collection<ParkingData> dataList) {
        List<CollectiveWeeklyParkingStats> result = new ArrayList<>();
        for (ParkingData data : dataList) {
            ParkingInfo info = new ParkingInfo(data.parkingId(), data.totalSpots());
            Map<DayOfWeek, Map<LocalTime, ParkingStats>> statsMap = new TreeMap<>();
            data.freeSpotsHistory().forEach((day, dailyHistory) -> {
                Map<LocalTime, ParkingStats> dailyStats = new TreeMap<>();
                statsMap.put(day, dailyStats);
                dailyHistory.forEach((key, value) -> {
                    double availability = value.averageAvailability();
                    ParkingStats stats = ParkingStats.builder()
                            .averageAvailability(round(availability))
                            .averageFreeSpots((int) (availability * data.totalSpots()))
                            .build();
                    dailyStats.put(key, stats);
                });
            });
            result.add(new CollectiveWeeklyParkingStats(info, statsMap));
        }
        return result;
    }

    private static double round(double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }
}
