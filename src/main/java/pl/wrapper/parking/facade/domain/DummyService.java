package pl.wrapper.parking.facade.domain;

import org.springframework.stereotype.Service;
import pl.wrapper.parking.infrastructure.error.Error;
import pl.wrapper.parking.infrastructure.error.ParkingError;
import pl.wrapper.parking.infrastructure.error.Result;

@Service
public class DummyService {

    public Result<String> dummyGetParkingBySymbol(String symbol, boolean willSucceed){
        if(willSucceed) return Result.success(symbol); //if success
        return Result.failure(new ParkingError.ParkingNotFoundBySymbol(symbol)); //if failure, return appropriate custom error

    }
}