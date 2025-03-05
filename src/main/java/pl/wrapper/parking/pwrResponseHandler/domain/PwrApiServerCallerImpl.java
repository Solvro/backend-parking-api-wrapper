package pl.wrapper.parking.pwrResponseHandler.domain;

import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class PwrApiServerCallerImpl implements PwrApiServerCaller {

    private static final int CACHE_TTL_MIN = 3;
    private final PwrApiCaller pwrApiCaller;

    @Override
    @Cacheable("parkingListCache")
    public List<ParkingResponse> fetchParkingData() {
        log.info("Fetching new data from Pwr api.");
        List<ParkingResponse> data = pwrApiCaller.fetchParkingPlaces().block();
        log.info("Data fetched successfully");
        return data;
    }

    @CacheEvict(
            value = {"parkingListCache", "chartCache"},
            allEntries = true)
    @Scheduled(fixedRate = CACHE_TTL_MIN, timeUnit = TimeUnit.MINUTES)
    public void flushCache() {
        log.info("Cache flushed. New data can be fetched.");
    }

    @Override
    @Cacheable("chartCache")
    public List<Object> getAllCharsForToday() {
        log.info("Fetching new chart data from Pwr api.");
        List<Object> charts = pwrApiCaller.fetchAllParkingCharts().block();
        log.info("Charts fetched successfully");
        return charts;
    }
}
