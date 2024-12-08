package pl.wrapper.parking.facade.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.facade.exception.AddressNotFoundException;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;

@WebFluxTest(ParkingController.class)
public class ParkingControllerTests {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParkingService parkingService;

    @Test
    void getAllParkings_returnsParkingList() throws IOException {
        List<ParkingResponse> parkings = List.of(
                ParkingResponse.builder()
                        .parkingId(1)
                        .name("Parking 1")
                        .symbol("P1")
                        .address(new Address("street 1", 37.1f, -158.8f))
                        .build(),
                ParkingResponse.builder()
                        .parkingId(2)
                        .name("Parking 2")
                        .symbol("P2")
                        .address(new Address("street 2", -44.4f, 123.6f))
                        .build()
        );
        when(parkingService.getAllParkings()).thenReturn(Mono.just(parkings));

        webTestClient.get().uri("/v1/parkings")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json(objectMapper.writeValueAsString(parkings));
    }

    @Test
    void getClosestParking_returnClosestParking() {
        String address = "test place";
        ParkingResponse parking = ParkingResponse.builder()
                .parkingId(1)
                .name("Parking 1")
                .symbol("P1")
                .address(new Address("street 1", 37.1f, -158.8f))
                .build();
        when(parkingService.getClosestParking(address)).thenReturn(Mono.just(parking));

        Flux<ParkingResponse> responseBody = webTestClient.get().uri(uriBuilder ->
                        uriBuilder
                                .path("/v1/parkings")
                                .queryParam("address", address)
                                .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(ParkingResponse.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .expectNext(parking)
                .verifyComplete();
    }

    @Test
    void getClosestParking_returnNotFound() {
        String address = "non-existent address";
        AddressNotFoundException error = new AddressNotFoundException(address);
        when(parkingService.getClosestParking(address)).thenReturn(Mono.error(error));

        webTestClient.get().uri(uriBuilder ->
                        uriBuilder
                                .path("/v1/parkings")
                                .queryParam("address", address)
                                .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.errorMessage").isEqualTo(error.getMessage());
    }

    @Test
    void getClosestParking_returnInternalServerError() {
        String address = "test place";
        NoSuchElementException error = new NoSuchElementException("No parkings available");
        when(parkingService.getClosestParking(address)).thenReturn(Mono.error(error));

        webTestClient.get().uri(uriBuilder ->
                        uriBuilder
                                .path("/v1/parkings")
                                .queryParam("address", address)
                                .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody()
                .jsonPath("$.errorMessage").isEqualTo(error.getMessage());
    }
}
