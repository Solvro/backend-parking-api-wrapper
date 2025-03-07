package pl.wrapper.parking.facade.domain.main;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrapper.parking.facade.dto.main.NominatimLocation;
import pl.wrapper.parking.infrastructure.error.ParkingError;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.infrastructure.nominatim.client.NominatimClient;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceImplTest {
    @Mock
    private PwrApiServerCaller pwrApiServerCaller;

    @Mock
    private NominatimClient nominatimClient;

    private ParkingServiceImpl parkingService;

    private List<ParkingResponse> parkings1;
    private List<ParkingResponse> parkings2;

    @BeforeEach
    void setUp() {
        parkingService = new ParkingServiceImpl(pwrApiServerCaller, nominatimClient);
        parkings1 = List.of(
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
                        .build());
        parkings2 = List.of(
                ParkingResponse.builder()
                        .parkingId(1)
                        .name("Parking 1")
                        .symbol("P1")
                        .freeSpots(0)
                        .openingHours(null)
                        .closingHours(null)
                        .build(),
                ParkingResponse.builder()
                        .parkingId(2)
                        .name("Parking 2")
                        .symbol("P2")
                        .freeSpots(325)
                        .openingHours(LocalTime.NOON)
                        .closingHours(LocalTime.NOON)
                        .build(),
                ParkingResponse.builder()
                        .parkingId(3)
                        .name("Parking 3")
                        .symbol("P3")
                        .freeSpots(117)
                        .openingHours(LocalTime.NOON)
                        .closingHours(LocalTime.NOON)
                        .build(),
                ParkingResponse.builder()
                        .parkingId(4)
                        .name("Parking 4")
                        .symbol("P4")
                        .freeSpots(51)
                        .openingHours(null)
                        .closingHours(null)
                        .build());
    }

    @Test
    void getClosestParking_returnSuccessWithClosestParking() {
        String address = "test place";
        NominatimLocation location = new NominatimLocation(37.0, -158.0);

        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.just(location));
        when(pwrApiServerCaller.fetchParkingData()).thenReturn(parkings1);

        Result<ParkingResponse> result = parkingService.getClosestParking(address);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).matches(p -> p.name().equals("Parking 1"));

        verify(nominatimClient).search(address, "json");
        verify(pwrApiServerCaller).fetchParkingData();
    }

    @Test
    void getClosestParking_returnFailureOfAddressNotFound_whenNoResultsFromApi() {
        String address = "non-existent address";

        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.empty());

        Result<ParkingResponse> result = parkingService.getClosestParking(address);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isInstanceOf(ParkingError.ParkingNotFoundByAddress.class);

        verify(nominatimClient).search(address, "json");
        verify(pwrApiServerCaller, never()).fetchParkingData();
    }

    @Test
    void getClosestParking_returnFailureOfAddressNotFound_whenNoParkingsAvailable() {
        String address = "test place";
        NominatimLocation location = new NominatimLocation(37.0, -158.0);

        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.just(location));
        when(pwrApiServerCaller.fetchParkingData()).thenReturn(Collections.emptyList());

        Result<ParkingResponse> result = parkingService.getClosestParking(address);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isInstanceOf(ParkingError.ParkingNotFoundByAddress.class);

        verify(nominatimClient).search(address, "json");
        verify(pwrApiServerCaller).fetchParkingData();
    }

    @Test()
    void getAllParkingsWithFreeSpots_shouldReturnList() {
        when(pwrApiServerCaller.fetchParkingData()).thenReturn(parkings2);
        List<ParkingResponse> result = parkingService.getAllWithFreeSpots(null);

        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(parking -> parking.freeSpots() > 0));
    }

    @Test
    void getOpenedParkingsWithFreeSpots_shouldReturnList() {
        when(pwrApiServerCaller.fetchParkingData()).thenReturn(parkings2);
        List<ParkingResponse> result = parkingService.getAllWithFreeSpots(true);

        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(parking -> parking.freeSpots() > 0 && parking.isOpened()));
    }

    @Test
    void getOpenedParkingsWithFreeSpots_shouldReturnEmptyList() {
        List<ParkingResponse> parkingDataLocal = new ArrayList<>(parkings2);
        parkingDataLocal.remove(3);

        when(pwrApiServerCaller.fetchParkingData()).thenReturn(parkingDataLocal);
        List<ParkingResponse> result = parkingService.getAllWithFreeSpots(true);

        assertEquals(0, result.size());
    }

    @Test
    void getClosedParkingsWithFreeSpots_shouldReturnList() {
        when(pwrApiServerCaller.fetchParkingData()).thenReturn(parkings2);
        List<ParkingResponse> result = parkingService.getAllWithFreeSpots(false);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(parking -> parking.freeSpots() > 0 && !parking.isOpened()));
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromAll_shouldReturnSuccessResult() {
        when(pwrApiServerCaller.fetchParkingData()).thenReturn(parkings2);
        Result<ParkingResponse> result = parkingService.getWithTheMostFreeSpots(null);

        assertTrue(result.isSuccess());
        assertEquals(325, result.getData().freeSpots());
        assertEquals("P2", result.getData().symbol());
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromOpened_shouldReturnSuccessResult() {
        when(pwrApiServerCaller.fetchParkingData()).thenReturn(parkings2);
        Result<ParkingResponse> result = parkingService.getWithTheMostFreeSpots(true);

        assertTrue(result.isSuccess());
        assertEquals(51, result.getData().freeSpots());
        assertEquals("P4", result.getData().symbol());
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromClosed_shouldReturnSuccessResult() {
        when(pwrApiServerCaller.fetchParkingData()).thenReturn(parkings2);
        Result<ParkingResponse> result = parkingService.getWithTheMostFreeSpots(false);

        assertTrue(result.isSuccess());
        assertEquals(325, result.getData().freeSpots());
        assertEquals("P2", result.getData().symbol());
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromClosed_shouldReturnNotFoundError() {
        List<ParkingResponse> parkingDataLocal = new ArrayList<>(parkings2);
        parkingDataLocal.remove(2);
        parkingDataLocal.remove(1);
        when(pwrApiServerCaller.fetchParkingData()).thenReturn(parkingDataLocal);
        Result<ParkingResponse> result = parkingService.getWithTheMostFreeSpots(false);

        assertFalse(result.isSuccess());
        assertInstanceOf(ParkingError.NoFreeParkingSpotsAvailable.class, result.getError());
    }
}
