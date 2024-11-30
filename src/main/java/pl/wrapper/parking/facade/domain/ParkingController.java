package pl.wrapper.parking.facade.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrapper.parking.facade.ParkingService;

@RequestMapping("v1")
@RestController
@RequiredArgsConstructor
class ParkingController {
    private final ParkingService parkingService;
}
