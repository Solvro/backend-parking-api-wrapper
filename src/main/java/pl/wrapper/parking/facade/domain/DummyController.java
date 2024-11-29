package pl.wrapper.parking.facade.domain;

import ch.qos.logback.core.joran.sanity.Pair;
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
    public ResponseEntity<String> getParkingOccupancyByParkingIdValid(
            HttpServletRequest request, @PathVariable("id") Long id) {
        boolean willSucceed = true;
        Result<Long> result = dummyService.dummyGetParkingBySymbol(id, willSucceed);
        return handleResult(result, HttpStatus.OK, request);
    }

    @Override
    protected Pair<HttpStatus, String> getInfoByError(Error error) {
        return switch (error) {
            case ParkingError.ParkingNotFoundBySymbol e -> new Pair<>(
                    HttpStatus.BAD_REQUEST, "Wrong Parking Symbol: " + e.symbol());
            case ParkingError.ParkingNotFoundById e -> new Pair<>(
                    HttpStatus.BAD_REQUEST, "Wrong Parking ID: " + e.id());
        };
    }
}
