package pl.wrapper.parking.facade.domain;

import org.springframework.stereotype.Service;
import pl.wrapper.parking.infrastructure.error.ParkingError;
import pl.wrapper.parking.infrastructure.error.Result;

@Service
public class DummyService {

    public Result<Long> dummyGetParkingBySymbol(Long id, boolean willSucceed) {
        if (willSucceed) return Result.success(id); // if success
        return Result.failure(new ParkingError.ParkingNotFoundById(id)); // if failure, return appropriate custom error
    }
}
