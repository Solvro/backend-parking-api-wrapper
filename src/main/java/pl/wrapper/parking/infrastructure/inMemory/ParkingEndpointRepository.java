package pl.wrapper.parking.infrastructure.inMemory;

import java.util.HashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//@Component("parkingEndpointRepository")
public class ParkingEndpointRepository extends InMemoryRepositoryImpl<Integer, DummyObject> { // <Key, value>

    public ParkingEndpointRepository(@Value("${serialization.location.ParkingRequests}") String saveToLocationPath) {
        super(
                saveToLocationPath, // to modify location path, change above @Value's value
                new HashMap<>(), // put here whatever map type you want
                null); // Add default value here (empty object probably)
    }
}
