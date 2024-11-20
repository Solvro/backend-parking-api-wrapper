package pl.wrapper.parking.exception;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrapper.parking.result.Result;


@RestController
@RequiredArgsConstructor
public class DummyController {
    private final DummyService parkingService;

    @GetMapping("/occupancy/by-value/valid")
    public Integer getParkingOccupancyByParkingIdValid() {
        Long id = 4L;
        Result<Integer> result = parkingService.dummyGetParkingOccupancyByParkingId(id);

        return result.getValue();
    }

    @GetMapping("/occupancy/by-value/invalid")
    public Integer getParkingOccupancyByParkingIdInvalid() {
        Long id = -4L;
        Result<Integer> result = parkingService.dummyGetParkingOccupancyByParkingId(id);

        return result.getValue();
    }
}