package pl.wrapper.parking.facade;

import pl.wrapper.parking.facade.dto.stats.request.EndpointStats;

import java.util.List;
import java.util.Map;

public interface ParkingRequestStatsService {
    Map<String, EndpointStats> getBasicRequestStats();

    Map<String, List<Map.Entry<String, Double>>> getRequestStatsForTimes();

    List<Map.Entry<String, Double>> getRequestPeakTimes();

    Map<String, Double> getDailyRequestStats();
}
