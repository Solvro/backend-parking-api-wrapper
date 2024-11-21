package pl.wrapper.parking.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrapper.parking.result.Result;


@RestController
@RequiredArgsConstructor
public class DummyController {
    private final DummyService parkingService;

    @GetMapping("/occupancy/by-value/valid")
    public ResponseEntity<Result<Integer>> getParkingOccupancyByParkingIdValid() {
        Long id = 4L;
        Result<Integer> result = parkingService.dummyGetParkingOccupancyByParkingId(id);

        return result.getResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/occupancy/by-value/invalid")
    public ResponseEntity<Result<Integer>> getParkingOccupancyByParkingIdInvalid() {
        Long id = -4L;
        Result<Integer> result = parkingService.dummyGetParkingOccupancyByParkingId(id);

        return result.getResponseEntity(HttpStatus.OK);
    }
}