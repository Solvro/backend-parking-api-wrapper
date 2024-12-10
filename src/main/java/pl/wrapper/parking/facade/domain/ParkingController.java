package pl.wrapper.parking.facade.domain;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.wrapper.parking.facade.ParkingService;

import static pl.wrapper.parking.infrastructure.error.HandleResult.*;

@RequestMapping("/v1/parkings")
@RestController
@RequiredArgsConstructor
@Slf4j
class ParkingController {
    private final ParkingService parkingService;

    @GetMapping
    public ResponseEntity<String> getAllParkings(HttpServletRequest request) {
        log.info("Fetching all parking lots.");
        return handleResult(parkingService.getAllParkings(), HttpStatus.OK, request.getRequestURI());
    }

    @GetMapping(params = "address")
    public ResponseEntity<String> getClosestParking(@RequestParam("address") String address, HttpServletRequest request) {
        log.info("Finding closest parking for address: {}", address);
        return handleResult(parkingService.getClosestParking(address), HttpStatus.OK, request.getRequestURI());
    }
}
