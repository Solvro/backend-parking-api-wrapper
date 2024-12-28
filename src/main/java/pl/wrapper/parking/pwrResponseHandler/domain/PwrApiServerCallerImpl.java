package pl.wrapper.parking.pwrResponseHandler.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.wrapper.parking.infrastructure.inMemory.ParkingDataRepository;
import pl.wrapper.parking.infrastructure.inMemory.dto.ParkingData;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class PwrApiServerCallerImpl implements PwrApiServerCaller {

    private static final int CACHE_TTL_MIN = 3;
    private final PwrApiCaller pwrApiCaller;
    private final ParkingDataRepository parkingDataRepository;

    @Override
    @Cacheable("parkingListCache")
    public List<ParkingResponse> fetchData() {
        log.info("Fetching new data from Pwr api.");
        List<ParkingResponse> parsedData = pwrApiCaller.fetchParkingPlaces().block();
        saveToRepo(parsedData);
        log.info("Fetch successful. Cache updated.");
        return parsedData;
    }

    private void saveToRepo(List<ParkingResponse> parsedData) {
        if(parsedData != null) {
            parsedData.stream()
                    .map(p -> 
                            new ParkingData(p.parkingId(), p.freeSpots(), p.totalSpots(), LocalDateTime.now()))
                    .forEach(data ->
                            parkingDataRepository.add(new ParkingData.CompositeKey(data.parkingId(), data.timestamp()), data));
        }
    }

    @CacheEvict("parkingListCache")
    @Scheduled(fixedRate = CACHE_TTL_MIN, timeUnit = TimeUnit.MINUTES)
    public void flushCache() {
        log.info("Cache flushed. New data can be fetched.");
    }
}
