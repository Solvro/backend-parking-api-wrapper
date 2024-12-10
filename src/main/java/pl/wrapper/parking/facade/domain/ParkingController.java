package pl.wrapper.parking.facade.domain;

import static pl.wrapper.parking.infrastructure.error.HandleResult.handleResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
class ParkingController {
    private final ParkingService parkingService;

    @Value("${server.servlet.context-path}")
    private String apiPath;

    @GetMapping("/name")
    public ResponseEntity<String> getParkingByName(@RequestParam String name, @RequestParam(required = false) Boolean opened) {
        log.info("Received request: get parking by name: {}", name);
        Result<ParkingResponse> result = parkingService.getByName(name, opened);
        return handleResult(result, HttpStatus.OK,  apiPath + "/name");
    }

    @GetMapping("/id")
    public ResponseEntity<String> getParkingById(@RequestParam Integer id, @RequestParam(required = false) Boolean opened) {
        log.info("Received request: get parking by id: {}", id);
        Result<ParkingResponse> result = parkingService.getById(id, opened);
        return handleResult(result, HttpStatus.OK, apiPath + "/id");
    }

    @GetMapping("/symbol")
    public ResponseEntity<String> getParkingBySymbol(@RequestParam String symbol, @RequestParam(required = false) Boolean opened) {
        log.info("Received request: get parking by symbol: {}", symbol);
        Result<ParkingResponse> result = parkingService.getBySymbol(symbol, opened);
        return handleResult(result, HttpStatus.OK,  apiPath + "/symbol");
    }

    @GetMapping
    public ResponseEntity<List<ParkingResponse>> getParkingByParams(@RequestParam(required = false) String symbol,
                                                     @RequestParam(required = false) Integer id,
                                                     @RequestParam(required = false) String name,
                                                     @RequestParam(required = false) Boolean opened) {
        log.info("Received request: get parking by symbol: {},id: {} and name: {}",symbol,id,name);
        List<ParkingResponse> result = parkingService.getByParams(symbol, id, name, opened);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
