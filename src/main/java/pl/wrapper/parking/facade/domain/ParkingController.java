package pl.wrapper.parking.facade.domain;

import static pl.wrapper.parking.infrastructure.error.HandleResult.handleResult;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

@RequestMapping("v1")
@RestController
@RequiredArgsConstructor
class ParkingController {
    private final ParkingService parkingService;

    @GetMapping("/name/{name}")
    public ResponseEntity<String> getParkingByName(@PathVariable String name) {
        Result<ParkingResponse> result = parkingService.getByName(name);
        return handleResult(result, HttpStatus.OK, "v1/name/{name}");
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<String> getParkingById(@PathVariable Integer id) {
        Result<ParkingResponse> result = parkingService.getById(id);
        return handleResult(result, HttpStatus.OK, "v1/name/{id}");
    }

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<String> getParkingBySymbol(@PathVariable String symbol) {
        Result<ParkingResponse> result = parkingService.getBySymbol(symbol);
        return handleResult(result, HttpStatus.OK, "v1/name/{symbol}");
    }
}
