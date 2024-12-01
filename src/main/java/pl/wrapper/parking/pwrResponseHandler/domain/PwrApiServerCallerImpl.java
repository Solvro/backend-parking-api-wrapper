package pl.wrapper.parking.pwrResponseHandler.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class PwrApiServerCallerImpl implements PwrApiServerCaller {

    private final static int CACHE_TTL_MIN = 3;
    private final PwrApiCaller pwrApiCaller;


    @Override
    @Cacheable("parkingListCache")
    public Mono<List<ParkingResponse>> fetchData() {
        log.info("Fetching new data from Pwr api.");
        Mono<List<ParkingResponse>> parsedData = pwrApiCaller.fetchParkingPlaces();
        log.info("Fetch successful. Cache updated.");
        return parsedData;
    }

    @CacheEvict("parkingListCache")
    @Scheduled(fixedRate = CACHE_TTL_MIN, timeUnit = TimeUnit.MINUTES)
    public void flushCache() {
        log.info("Cache flushed. New data can be fetched.");
    }
}
