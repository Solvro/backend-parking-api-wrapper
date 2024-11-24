package pl.wrapper.parking.facade.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@RequestMapping("/v1/parkings")
@RestController
@RequiredArgsConstructor
@Slf4j
class ParkingController {
    private final ParkingService parkingService;

    @GetMapping
    public Mono<List<ParkingResponse>> getAllParkings() {
        log.info("Fetching all parking lots.");
        return parkingService.getAllParkings();
    }

    @GetMapping(params = "address")
    public Mono<ParkingResponse> getClosestParking(@RequestParam("address") String address) {
        log.info("Finding closest parking for address: {}", address);
        return parkingService.getClosestParking(address);
    }
}
