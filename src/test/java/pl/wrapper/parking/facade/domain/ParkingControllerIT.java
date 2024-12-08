package pl.wrapper.parking.facade.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import pl.wrapper.parking.facade.client.NominatimClient;
import pl.wrapper.parking.facade.dto.NominatimLocation;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ParkingControllerIT {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PwrApiServerCaller pwrApiServerCaller;

    @MockBean
    private NominatimClient nominatimClient;

    private List<ParkingResponse> parkings;

    @BeforeEach
    void setUp() {
        parkings = List.of(
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
    }

    @Test
    void getAllParkings_returnParkingList() throws IOException {
        when(pwrApiServerCaller.fetchData()).thenReturn(Mono.just(parkings));

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
        NominatimLocation location = new NominatimLocation(37.0, -158.0);

        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.just(location));
        when(pwrApiServerCaller.fetchData()).thenReturn(Mono.just(parkings));

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
                .expectNextMatches(p -> p.name().equals("Parking 1"))
                .verifyComplete();
    }

    @Test
    void getClosestParking_returnNotFound() {
        String address = "non-existent address";
        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.empty());

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
                .jsonPath("$.errorMessage")
                .value(CoreMatchers.containsString(address));

        verify(pwrApiServerCaller, never()).fetchData();
    }

    @Test
    void getClosestParking_returnInternalServerError(@Value("${general.exception.message}") String errorMessage) {
        String address = "test place";
        NominatimLocation location = new NominatimLocation(37.0, -158.0);
        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.just(location));
        when(pwrApiServerCaller.fetchData()).thenReturn(Mono.just(Collections.emptyList()));

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
                .jsonPath("$.errorMessage")
                .value(CoreMatchers.is(errorMessage));
    }
}
