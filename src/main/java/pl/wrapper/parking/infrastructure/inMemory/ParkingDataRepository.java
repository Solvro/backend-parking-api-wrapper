package pl.wrapper.parking.infrastructure.inMemory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.wrapper.parking.infrastructure.inMemory.dto.ParkingData;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

@Component("parkingDataRepository")
public class ParkingDataRepository extends InMemoryRepositoryImpl<ParkingData.CompositeKey, ParkingData> {

    private PwrApiServerCaller pwrApiServerCaller;

    public ParkingDataRepository(@Value("${serialization.location.parkingData}") String saveToLocationPath) {
        super(
                saveToLocationPath,
                new HashMap<>(),
                null);
    }

    @Autowired
    public void setPwrApiServerCaller(PwrApiServerCaller pwrApiServerCaller) {
        this.pwrApiServerCaller = pwrApiServerCaller;
    }

    @Scheduled(fixedRateString = "${pwr-api.data-fetch.minutes}", timeUnit = TimeUnit.MINUTES)
    private void handleData() {
        List<ParkingResponse> parkings = pwrApiServerCaller.fetchData();
        parkings.stream()
                .map(p ->
                        new ParkingData(p.parkingId(), p.freeSpots(), p.totalSpots(), LocalDateTime.now()))
                .forEach(data ->
                        add(new ParkingData.CompositeKey(data.parkingId(), data.timestamp()), data));
    }
}
