package pl.wrapper.parking.facade.domain;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pl.wrapper.parking.infrastructure.error.*;
import pl.wrapper.parking.infrastructure.error.Error;

@RestController
@RequiredArgsConstructor
public class DummyController extends HandleResult {
    private final DummyService dummyService;

    @GetMapping("/id/{id}")
    public ResponseEntity<String> getParkingOccupancyByParkingIdValid(@PathVariable("id") Long id) {
        boolean willSucceed = true;
        Result<Long> result = dummyService.dummyGetParkingBySymbol(id, willSucceed);
        return handleResult(result, HttpStatus.OK, "/id/" + id);
    }

    @Override
    protected ErrorWrapper getInfoByError(Error error, String uri, HttpStatus onSuccess) {
        return switch (error) {
            case ParkingError.ParkingNotFoundBySymbol e -> new ErrorWrapper(
                    "Wrong Parking Symbol: " + e.symbol(), onSuccess, uri, HttpStatus.BAD_REQUEST);
            case ParkingError.ParkingNotFoundById e -> new ErrorWrapper(
                    "Wrong Parking Id: " + e.id(), onSuccess, uri, HttpStatus.BAD_REQUEST);
        };
    }
}
