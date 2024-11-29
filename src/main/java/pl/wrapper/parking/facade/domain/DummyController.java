package pl.wrapper.parking.facade.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pl.wrapper.parking.infrastructure.error.*;

import static pl.wrapper.parking.infrastructure.error.HandleResult.handleResult;

@RestController
@RequiredArgsConstructor
public class DummyController {
    private final DummyService dummyService;

    @GetMapping("/id/{id}")
    public ResponseEntity<String> getParkingOccupancyByParkingIdValid(@PathVariable("id") Long id) {
        boolean willSucceed = true;
        Result<Long> result = dummyService.dummyGetParkingBySymbol(id, willSucceed);
        return handleResult(result, HttpStatus.OK, "/id/" + id);
    }
}
