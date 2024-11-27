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
    public ResponseEntity<String> getParkingOccupancyByParkingIdValid(HttpServletRequest request,
                                                                      @PathVariable("id") Long id) {
        boolean willSucceed = false;
        Result<Long> result = dummyService.dummyGetParkingBySymbol(id, willSucceed);
        return handleResult(result, HttpStatus.OK, request);
    }


    protected HttpStatus getStatusByError(Error error){
        return switch (error){
            case ParkingError.ParkingNotFoundBySymbol e -> HttpStatus.BAD_REQUEST;
            case ParkingError.ParkingNotFoundById e -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    protected String getMessageByError(Error error){
        return switch (error){
            case ParkingError.ParkingNotFoundBySymbol e -> "Wrong Parking Symbol: " + e.symbol();
            case ParkingError.ParkingNotFoundById e -> "Wrong Parking ID: " + e.id();
            default -> "An error has occured";
        };
    }
}