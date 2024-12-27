package pl.wrapper.parking.infrastructure.inMemory;

import java.util.HashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("parkingDataRepository")
public class ParkingDataRepository extends InMemoryRepositoryImpl<Integer, DummyObject> { // <Key, value>

    public ParkingDataRepository(@Value("${serialization.location.parkingData}") String saveToLocationPath) {
        super(
                saveToLocationPath, // to modify location path, change above @Value's value
                new HashMap<>(), // put here whatever map type you want
                null); // Add default value here (empty object probably)
    }
}
