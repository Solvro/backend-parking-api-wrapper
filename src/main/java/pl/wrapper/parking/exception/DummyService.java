package pl.wrapper.parking.exception;

import org.springframework.stereotype.Service;
import pl.wrapper.parking.result.FailureResult;
import pl.wrapper.parking.result.Result;
import pl.wrapper.parking.result.SuccessResult;

import static pl.wrapper.parking.result.FailureResult.failureResult;

@Service
public class DummyService {

    public Result<String> dummyGetParkingById(Long id){
        if(id < 0)
            return new FailureResult<>(new IndexOutOfBoundsException(id));

        return new SuccessResult<>("Cupcake Parking " + id);
    }

    public Result<Integer> dummyGetParkingOccupancyByParkingId(Long id) {
        Result<String> parkingResult = dummyGetParkingById(id);

        if (parkingResult.isSuccess())
            return new SuccessResult<>(parkingResult.getValue().length());

        return failureResult(parkingResult);
    }
}