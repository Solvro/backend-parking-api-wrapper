package pl.wrapper.parking.facade.domain.stats.request;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import pl.wrapper.parking.facade.ParkingRequestStatsService;
import pl.wrapper.parking.facade.dto.stats.request.EndpointStats;
import pl.wrapper.parking.infrastructure.inMemory.ParkingRequestRepository;
import pl.wrapper.parking.infrastructure.inMemory.dto.request.EndpointData;
import pl.wrapper.parking.infrastructure.inMemory.dto.request.TimeframeStatistic;

@Service
class ParkingRequestStatsServiceImpl implements ParkingRequestStatsService {

    private final ParkingRequestRepository requestRepository;

    private final List<String> formattedTimeframes;

    public ParkingRequestStatsServiceImpl(ParkingRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
        this.formattedTimeframes = getFormattedTimeframes(
                requestRepository.getTotalEndpoint().getTimeframeLength(),
                requestRepository.getTotalEndpoint().getTimeframeStatistics().length);
    }

    @Override
    public Map<String, EndpointStats> getBasicRequestStats() {
        return requestRepository.fetchAllEntries().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            EndpointData endpointData = entry.getValue();
            return new EndpointStats(
                    endpointData.getRequestCount(), endpointData.getSuccessCount(), endpointData.getSuccessRate());
        }));
    }

    @Override
    public Map<String, List<Map.Entry<String, Double>>> getRequestStatsForTimes() {
        return requestRepository.fetchAllEntries().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> getTimeframesWithAverage(entry.getValue())));
    }

    @Override
    public List<Map.Entry<String, Double>> getRequestPeakTimes() {
        EndpointData totalEndpoint = requestRepository.getTotalEndpoint();
        List<Map.Entry<String, Double>> timeframesWithAverage = getTimeframesWithAverage(totalEndpoint);

        return timeframesWithAverage.stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .toList();
    }

    @Override
    public Map<String, Double> getDailyRequestStats() {
        return requestRepository.fetchAllEntries().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            EndpointData endpointData = entry.getValue();
            double sumOfAverages = 0.0;
            for (TimeframeStatistic timeframeStatistic : endpointData.getTimeframeStatistics()) {
                sumOfAverages += timeframeStatistic.getAverageNumberOfRequests();
            }
            return sumOfAverages;
        }));
    }

    private List<Map.Entry<String, Double>> getTimeframesWithAverage(EndpointData endpointData) {
        List<Map.Entry<String, Double>> averages = new ArrayList<>();
        for (int i = 0; i < endpointData.getTimeframeStatistics().length; ++i) {
            Double average = endpointData.getTimeframeStatistics()[i].getAverageNumberOfRequests();
            averages.add(Map.entry(formattedTimeframes.get(i), average));
        }
        return averages;
    }

    private List<String> getFormattedTimeframes(int timeframeLength, int maxTimeframesCount) {
        List<String> timeframes = new ArrayList<>();
        for (int i = 0; i < maxTimeframesCount; ++i) {
            LocalTime start = LocalTime.MIDNIGHT.plusMinutes((long) i * timeframeLength);
            LocalTime end = start.plusMinutes(timeframeLength);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            timeframes.add(formatter.format(start) + " - " + formatter.format(end));
        }
        return timeframes;
    }
}
