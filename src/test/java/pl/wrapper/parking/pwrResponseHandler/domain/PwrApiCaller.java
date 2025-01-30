package pl.wrapper.parking.pwrResponseHandler.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.util.List;

@Profile("test")
@Component
@RequiredArgsConstructor
public final class PwrApiCaller {

    public Mono<List<ParkingResponse>> fetchParkingPlaces() {
        return Mono.just(List.of(
                ParkingResponse.builder()
                        .parkingId(1)
                        .freeSpots(15)
                        .totalSpots(50)
                        .name("Central Parking")
                        .symbol("CEN")
                        .openingHours(LocalTime.of(8, 0))
                        .closingHours(LocalTime.of(20, 0))
                        .address(new Address("Main St", 10.21f, 4.32f))
                        .build(),
                ParkingResponse.builder()
                        .parkingId(2)
                        .freeSpots(5)
                        .totalSpots(30)
                        .name("Westside Parking")
                        .symbol("WSP")
                        .openingHours(LocalTime.of(6, 0))
                        .closingHours(LocalTime.of(22, 0))
                        .address(new Address("West St", 10f, -4f))
                        .build(),
                ParkingResponse.builder()
                        .parkingId(3)
                        .freeSpots(0)
                        .totalSpots(100)
                        .name("Airport Parking")
                        .symbol("AIR")
                        .openingHours(null)
                        .closingHours(null)
                        .address(new Address("Airport Rd", 13.0f, 2.0f))
                        .build(),
                ParkingResponse.builder()
                        .parkingId(4)
                        .freeSpots(10)
                        .totalSpots(60)
                        .name("Eastside Parking")
                        .symbol("ESP")
                        .openingHours(LocalTime.of(7, 30))
                        .closingHours(LocalTime.of(21, 30))
                        .address(new Address("East St", 12.0f, 12.0f))
                        .build(),
                ParkingResponse.builder()
                        .parkingId(5)
                        .freeSpots(25)
                        .totalSpots(50)
                        .name("Downtown Parking")
                        .symbol("DTP")
                        .openingHours(LocalTime.of(8, 0))
                        .closingHours(LocalTime.of(18, 0))
                        .address(new Address("Downtown Ln", 0.3f, 2.1f))
                        .build())
        );
    }
}