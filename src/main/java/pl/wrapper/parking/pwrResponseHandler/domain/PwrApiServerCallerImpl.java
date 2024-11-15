package pl.wrapper.parking.pwrResponseHandler.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
class PwrApiServerCallerImpl implements PwrApiServerCaller {

    private final static long CACHE_TTL_MS = 180000L;
    private final PwrApiCaller pwrApiCaller;


    @Override
    @Cacheable("parkingListCache")
    public List<ParkingResponse> fetchData() {
        log.info("Fetching new data from Pwr api.");
        List<ParkingResponse> parsedData = pwrApiCaller.fetchParkingPlaces().block();
        log.info("Fetch successful. Cache updated.");
        return parsedData;
    }

    @CacheEvict("parkingListCache")
    @Scheduled(fixedDelay = CACHE_TTL_MS)
    public void flushCache() {
        log.info("Cache flushed. New data can be fetched.");
    }
}
