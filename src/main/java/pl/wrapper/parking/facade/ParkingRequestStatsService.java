package pl.wrapper.parking.facade;

import java.util.List;
import java.util.Map;
import pl.wrapper.parking.facade.dto.stats.request.EndpointStats;

public interface ParkingRequestStatsService {
    Map<String, EndpointStats> getBasicRequestStats();

    Map<String, List<Map.Entry<String, Double>>> getRequestStatsForTimes();

    List<Map.Entry<String, Double>> getRequestPeakTimes();

    Map<String, Double> getDailyRequestStats();
}
