package pl.wrapper.parking.facade.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrapper.parking.infrastructure.error.ParkingError;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ParkingServiceImplFreeSpotsTest {
    @Mock
    private PwrApiServerCaller pwrApiServerCaller;

    @InjectMocks
    private ParkingServiceImpl parkingService;

    private List<ParkingResponse> parkingData;

    @BeforeEach
    void setUp() {
        parkingData = List.of(
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
                        .build()
        );
    }

    @Test()
    void getAllParkingsWithFreeSpots_shouldReturnList() {
        when(pwrApiServerCaller.fetchData()).thenReturn(parkingData);
        List<ParkingResponse> result = parkingService.getAllWithFreeSpots(null);

        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(parking -> parking.freeSpots() > 0));
    }

    @Test
    void getOpenedParkingsWithFreeSpots_shouldReturnList() {
        when(pwrApiServerCaller.fetchData()).thenReturn(parkingData);
        List<ParkingResponse> result = parkingService.getAllWithFreeSpots(true);

        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(parking -> parking.freeSpots() > 0 && parking.isOpened()));
    }

    @Test
    void getOpenedParkingsWithFreeSpots_shouldReturnEmptyList() {
        List<ParkingResponse> parkingDataLocal = new ArrayList<>(parkingData);
        parkingDataLocal.remove(3);

        when(pwrApiServerCaller.fetchData()).thenReturn(parkingDataLocal);
        List<ParkingResponse> result = parkingService.getAllWithFreeSpots(true);

        assertEquals(0, result.size());
    }

    @Test
    void getClosedParkingsWithFreeSpots_shouldReturnList() {
        when(pwrApiServerCaller.fetchData()).thenReturn(parkingData);
        List<ParkingResponse> result = parkingService.getAllWithFreeSpots(false);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(parking -> parking.freeSpots() > 0 && !parking.isOpened()));
    }


    @Test
    void getParkingWithTheMostFreeSpacesFromAll_shouldReturnSuccessResult() {
        when(pwrApiServerCaller.fetchData()).thenReturn(parkingData);
        Result<ParkingResponse> result = parkingService.getWithTheMostFreeSpots(null);

        assertTrue(result.isSuccess());
        assertEquals(325, result.getData().freeSpots());
        assertEquals("P2", result.getData().symbol());
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromOpened_shouldReturnSuccessResult() {
        when(pwrApiServerCaller.fetchData()).thenReturn(parkingData);
        Result<ParkingResponse> result = parkingService.getWithTheMostFreeSpots(true);

        assertTrue(result.isSuccess());
        assertEquals(51, result.getData().freeSpots());
        assertEquals("P4", result.getData().symbol());
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromClosed_shouldReturnSuccessResult() {
        when(pwrApiServerCaller.fetchData()).thenReturn(parkingData);
        Result<ParkingResponse> result = parkingService.getWithTheMostFreeSpots(false);

        assertTrue(result.isSuccess());
        assertEquals(325, result.getData().freeSpots());
        assertEquals("P2", result.getData().symbol());
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromClosed_shouldReturnNotFoundError() {
        List<ParkingResponse> parkingDataLocal = new ArrayList<>(parkingData);
        parkingDataLocal.remove(2);
        parkingDataLocal.remove(1);
        when(pwrApiServerCaller.fetchData()).thenReturn(parkingDataLocal);
        Result<ParkingResponse> result = parkingService.getWithTheMostFreeSpots(false);

        assertFalse(result.isSuccess());
        assertInstanceOf(ParkingError.ParkingWithTheMostFreeSpotsNotFound.class, result.getError());
    }
}