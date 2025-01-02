package pl.wrapper.parking.facade.domain;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrapper.parking.facade.dto.NominatimLocation;
import pl.wrapper.parking.facade.dto.ParkingStatsResponse;
import pl.wrapper.parking.infrastructure.error.ParkingError;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.infrastructure.inMemory.ParkingDataRepository;
import pl.wrapper.parking.infrastructure.inMemory.dto.ParkingData;
import pl.wrapper.parking.infrastructure.nominatim.client.NominatimClient;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceImplTests {
    @Mock
    private PwrApiServerCaller pwrApiServerCaller;

    @Mock
    private NominatimClient nominatimClient;

    @Mock
    private ParkingDataRepository dataRepository;

    @InjectMocks
    private ParkingServiceImpl parkingService;

    private List<ParkingResponse> parkings;
    private List<ParkingResponse> parkingData;
    private List<ParkingData> dataList;

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
                        .build());
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
                        .build());
        dataList = List.of(
                ParkingData.builder()
                        .parkingId(1)
                        .freeSpots(5)
                        .totalSpots(10)
                        .timestamp(LocalDateTime.of(2024, 12, 7, 15, 45))
                        .build(),
                ParkingData.builder()
                        .parkingId(1)
                        .freeSpots(4)
                        .totalSpots(20)
                        .timestamp(LocalDateTime.of(2025, 1, 1, 19, 23))
                        .build(),
                ParkingData.builder()
                        .parkingId(2)
                        .freeSpots(20)
                        .totalSpots(30)
                        .timestamp(LocalDateTime.of(2024, 12, 4, 23, 35))
                        .build());
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
        assertInstanceOf(ParkingError.NoFreeParkingSpotsAvailable.class, result.getError());
    }

    @Test
    void getParkingStats_withValidDateTimeRangeAndParkingId_returnStatsForParkingWithinRange() {
        int parkingId = 1;
        LocalDateTime start = LocalDateTime.of(2024, 12, 7, 15, 45);
        LocalDateTime end = LocalDateTime.of(2025, 1, 19, 4, 0);
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);
        when(dataRepository.values()).thenReturn(dataList);

        Result<ParkingStatsResponse> result = parkingService.getParkingStats(parkingId, start, end);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData())
                .extracting(
                        ParkingStatsResponse::totalUsage,
                        ParkingStatsResponse::averageAvailability,
                        ParkingStatsResponse::peakOccupancyAt)
                .containsExactly(21L, 0.35, dataList.get(1).timestamp());
    }

    @Test
    void getParkingStats_withValidDateTimeRange_returnStatsForParkingsWithinRange() {
        LocalDateTime start = LocalDateTime.of(2024, 12, 1, 15, 45);
        LocalDateTime end = LocalDateTime.of(2025, 1, 19, 4, 0);
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);
        when(dataRepository.values()).thenReturn(dataList);

        Result<ParkingStatsResponse> result = parkingService.getParkingStats(null, start, end);

        assertTrue(result.isSuccess());
        ParkingStatsResponse stats = result.getData();
        assertEquals(31L, stats.totalUsage());
        assertEquals(0.455, stats.averageAvailability(), 0.005);
        assertEquals(dataList.get(1).timestamp(), stats.peakOccupancyAt());
    }

    @Test
    void getParkingStats_withValidParkingIdAndNoDateTime_returnStatsWithinWholeRange() {
        int parkingId = 1;
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);
        when(dataRepository.values()).thenReturn(dataList);

        Result<ParkingStatsResponse> result = parkingService.getParkingStats(parkingId, (LocalDateTime) null, null);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData())
                .extracting(
                        ParkingStatsResponse::totalUsage,
                        ParkingStatsResponse::averageAvailability,
                        ParkingStatsResponse::peakOccupancyAt)
                .containsExactly(21L, 0.35, dataList.get(1).timestamp());
    }

    @Test
    void getParkingStats_withValidStartDateTimeAndNoParkingId_returnStatsFromStartDateTime() {
        LocalDateTime start = LocalDateTime.of(2024, 12, 1, 0,0);
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);
        when(dataRepository.values()).thenReturn(dataList);

        Result<ParkingStatsResponse> result = parkingService.getParkingStats(null, start, null);

        assertTrue(result.isSuccess());
        ParkingStatsResponse stats = result.getData();
        assertEquals(31L, stats.totalUsage());
        assertEquals(0.455, stats.averageAvailability(), 0.005);
        assertEquals(dataList.get(1).timestamp(), stats.peakOccupancyAt());
    }

    @Test
    void getParkingStats_withValidEndDateTimeAndParkingId_returnStatsUpToEndDateTime() {
        int parkingId = 1;
        LocalDateTime end = LocalDateTime.of(2025, 12, 1, 0,0);
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);
        when(dataRepository.values()).thenReturn(dataList);

        Result<ParkingStatsResponse> result = parkingService.getParkingStats(parkingId, null, end);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData())
                .extracting(
                        ParkingStatsResponse::totalUsage,
                        ParkingStatsResponse::averageAvailability,
                        ParkingStatsResponse::peakOccupancyAt)
                .containsExactly(21L, 0.35, dataList.get(1).timestamp());
    }

    @Test
    void getParkingStats_withNoParkingIdAndDateTime_returnStatsForAllParkingsWithinWholeRange() {
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);
        when(dataRepository.values()).thenReturn(dataList);

        Result<ParkingStatsResponse> result = parkingService.getParkingStats(null, (LocalDateTime) null, null);

        assertTrue(result.isSuccess());
        ParkingStatsResponse stats = result.getData();
        assertEquals(31L, stats.totalUsage());
        assertEquals(0.455, stats.averageAvailability(), 0.005);
        assertEquals(dataList.get(1).timestamp(), stats.peakOccupancyAt());
    }

    @Test
    void getParkingStats_withIncorrectDateTimeRange_returnNullStats() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 0, 0);
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);
        when(dataRepository.values()).thenReturn(dataList);

        Result<ParkingStatsResponse> result = parkingService.getParkingStats(null, start, end);

        assertTrue(result.isSuccess());
        ParkingStatsResponse stats = result.getData();
        assertThat(stats.totalUsage()).isZero();
        assertThat(stats.averageAvailability()).isZero();
        assertThat(stats.peakOccupancyAt()).isNull();
    }

    @Test
    void getParkingStats_withIncorrectParkingIdAndValidDateTime_returnFailureOfIdNotFound() {
        int parkingId = -1;
        LocalDateTime start = LocalDateTime.of(2024, 12, 7, 15, 45);
        LocalDateTime end = LocalDateTime.of(2025, 1, 19, 4, 0);
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);

        Result<ParkingStatsResponse> result = parkingService.getParkingStats(parkingId, start, end);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isInstanceOf(ParkingError.ParkingNotFoundById.class);

        verify(dataRepository, never()).values();
    }

    @Test
    void getParkingStats_withValidParkingIdAndTimeRange_returnStatsForParkingWithinRange() {
        int parkingId = 1;
        LocalTime startTime = LocalTime.of(17, 15, 45);
        LocalTime endTime = LocalTime.of(23, 35);
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);
        when(dataRepository.values()).thenReturn(dataList);

        Result<ParkingStatsResponse> result = parkingService.getParkingStats(parkingId, startTime, endTime);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData())
                .extracting(
                        ParkingStatsResponse::totalUsage,
                        ParkingStatsResponse::averageAvailability,
                        ParkingStatsResponse::peakOccupancyAt)
                .containsExactly(16L, 0.2, dataList.get(1).timestamp());
    }

    @Test
    void getParkingStats_withValidParkingIdAndNoTimeRange_returnStatsForParkingWithinRange() {
        int parkingId = 1;
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);
        when(dataRepository.values()).thenReturn(dataList);

        Result<ParkingStatsResponse> result = parkingService.getParkingStats(parkingId, (LocalTime) null, null);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData())
                .extracting(
                        ParkingStatsResponse::totalUsage,
                        ParkingStatsResponse::averageAvailability,
                        ParkingStatsResponse::peakOccupancyAt)
                .containsExactly(21L, 0.35, dataList.get(1).timestamp());
    }
}
