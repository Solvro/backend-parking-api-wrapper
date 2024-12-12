package pl.wrapper.parking.facade.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrapper.parking.facade.client.NominatimClient;
import pl.wrapper.parking.facade.dto.NominatimLocation;
import pl.wrapper.parking.infrastructure.error.ParkingError;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceImplTests {
    @Mock
    private PwrApiServerCaller pwrApiServerCaller;

    @Mock
    private NominatimClient nominatimClient;

    @InjectMocks
    private ParkingServiceImpl parkingService;

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
    void getClosestParking_returnSuccessWithClosestParking() {
        String address = "test place";
        NominatimLocation location = new NominatimLocation(37.0, -158.0);

        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.just(location));
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);

        Result<ParkingResponse> result = parkingService.getClosestParking(address);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).matches(p -> p.name().equals("Parking 1"));

        verify(nominatimClient).search(address, "json");
        verify(pwrApiServerCaller).fetchData();
    }

    @Test
    void getClosestParking_returnFailureOfAddressNotFound_whenNoResultsFromApi() {
        String address = "non-existent address";

        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.empty());

        Result<ParkingResponse> result = parkingService.getClosestParking(address);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isInstanceOf(ParkingError.ParkingNotFoundByAddress.class);

        verify(nominatimClient).search(address, "json");
        verify(pwrApiServerCaller, never()).fetchData();
    }

    @Test
    void getClosestParking_returnFailureOfAddressNotFound_whenNoParkingsAvailable() {
        String address = "test place";
        NominatimLocation location = new NominatimLocation(37.0, -158.0);

        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.just(location));
        when(pwrApiServerCaller.fetchData()).thenReturn(Collections.emptyList());

        Result<ParkingResponse> result = parkingService.getClosestParking(address);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isInstanceOf(ParkingError.ParkingNotFoundByAddress.class);

        verify(nominatimClient).search(address, "json");
        verify(pwrApiServerCaller).fetchData();
    }
}
