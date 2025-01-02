package pl.wrapper.parking.facade.domain;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.facade.dto.ParkingStatsResponse;
import pl.wrapper.parking.infrastructure.error.ParkingError;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

@WebMvcTest(ParkingController.class)
public class ParkingControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingService parkingService;

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
                        .build());
    }

    @Test
    void getClosestParking_returnClosestParking() throws Exception {
        String address = "test place";
        ParkingResponse parking = ParkingResponse.builder()
                .parkingId(1)
                .name("Parking 1")
                .symbol("P1")
                .address(new Address("street 1", 37.1f, -158.8f))
                .build();
        when(parkingService.getClosestParking(address)).thenReturn(Result.success(parking));

        mockMvc.perform(get("/parkings/address").queryParam("address", address).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parkingId", is(parking.parkingId())))
                .andExpect(jsonPath("$.name", is(parking.name())))
                .andExpect(jsonPath("$.address.geoLatitude")
                        .value(parking.address().geoLatitude()))
                .andExpect(jsonPath("$.address.geoLongitude")
                        .value(parking.address().geoLongitude()));
    }

    @Test
    void getClosestParking_returnNotFound() throws Exception {
        String address = "non-existent address";
        ParkingError.ParkingNotFoundByAddress error = new ParkingError.ParkingNotFoundByAddress(address);
        when(parkingService.getClosestParking(address)).thenReturn(Result.failure(error));

        mockMvc.perform(get("/parkings/address").queryParam("address", address).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }

    @Test()
    void getAllParkingsWithFreeSpots_shouldReturnListOfParking() throws Exception {
        List<ParkingResponse> serviceResponse = List.of(parkingData.get(1), parkingData.get(2), parkingData.get(3));
        when(parkingService.getAllWithFreeSpots(null)).thenReturn(serviceResponse);

        String jsonResponse = mockMvc.perform(get("/parkings/free").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONArray jsonArray = new JSONArray(jsonResponse);

        assertEquals(3, jsonArray.length());
        for (int i = 0; i < jsonArray.length(); ++i) {
            assertTrue(jsonArray.getJSONObject(i).getInt("freeSpots") > 0);
        }
    }

    @Test
    void getOpenedParkingsWithFreeSpots_shouldReturnListOfParking() throws Exception {
        List<ParkingResponse> serviceResponse = List.of(parkingData.get(3));
        when(parkingService.getAllWithFreeSpots(true)).thenReturn(serviceResponse);

        String jsonResponse = mockMvc.perform(
                        get("/parkings/free").queryParam("opened", "true").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONArray jsonArray = new JSONArray(jsonResponse);

        assertEquals(1, jsonArray.length());
        for (int i = 0; i < jsonArray.length(); ++i) {
            assertTrue(jsonArray.getJSONObject(i).getInt("freeSpots") > 0);
        }
    }

    @Test
    void getOpenedParkingsWithFreeSpots_shouldReturnEmptyListOfParking() throws Exception {
        List<ParkingResponse> serviceResponse = List.of();
        when(parkingService.getAllWithFreeSpots(true)).thenReturn(serviceResponse);

        String jsonResponse = mockMvc.perform(
                        get("/parkings/free").queryParam("opened", "true").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONArray jsonArray = new JSONArray(jsonResponse);
        assertEquals(0, jsonArray.length());
    }

    @Test
    void getClosedParkingsWithFreeSpots_shouldReturnListOfParking() throws Exception {
        List<ParkingResponse> serviceResponse = List.of(parkingData.get(1), parkingData.get(2));
        when(parkingService.getAllWithFreeSpots(false)).thenReturn(serviceResponse);

        String jsonResponse = mockMvc.perform(
                        get("/parkings/free").queryParam("opened", "false").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONArray jsonArray = new JSONArray(jsonResponse);

        assertEquals(2, jsonArray.length());
        for (int i = 0; i < jsonArray.length(); ++i) {
            assertTrue(jsonArray.getJSONObject(i).getInt("freeSpots") > 0);
        }
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromAll_shouldReturnParking() throws Exception {
        Result<ParkingResponse> serviceResponse = Result.success(parkingData.get(1));
        when(parkingService.getWithTheMostFreeSpots(null)).thenReturn(serviceResponse);

        mockMvc.perform(get("/parkings/free/top").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeSpots").value(325))
                .andExpect(jsonPath("$.symbol").value("P2"));
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromOpened_shouldReturnParking() throws Exception {
        Result<ParkingResponse> serviceResponse = Result.success(parkingData.get(3));
        when(parkingService.getWithTheMostFreeSpots(true)).thenReturn(serviceResponse);

        mockMvc.perform(get("/parkings/free/top").queryParam("opened", "true").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeSpots").value(51))
                .andExpect(jsonPath("$.symbol").value("P4"));
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromClosed_shouldReturnParking() throws Exception {
        Result<ParkingResponse> serviceResponse = Result.success(parkingData.get(1));
        when(parkingService.getWithTheMostFreeSpots(false)).thenReturn(serviceResponse);

        mockMvc.perform(get("/parkings/free/top").queryParam("opened", "false").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeSpots").value(325))
                .andExpect(jsonPath("$.symbol").value("P2"));
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromClosed_shouldReturnNotFound() throws Exception {
        Result<ParkingResponse> serviceResponse = Result.failure(new ParkingError.NoFreeParkingSpotsAvailable());
        when(parkingService.getWithTheMostFreeSpots(false)).thenReturn(serviceResponse);

        mockMvc.perform(get("/parkings/free/top").queryParam("opened", "false").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }

    @Test
    void getParkingStats_withValidParkingIdAndDateTimeRange_returnParkingStatsForParkingWithinRange() throws Exception {
        int parkingId = 1;
        LocalDateTime start = LocalDateTime.of(2024, 12, 7, 15, 45);
        LocalDateTime end = LocalDateTime.of(2025, 1, 19, 4, 0);
        ParkingStatsResponse stats = new ParkingStatsResponse(21L, 0.35, LocalDateTime.now());
        when(parkingService.getParkingStats(parkingId, start, end)).thenReturn(Result.success(stats));

        mockMvc.perform(get("/parkings/stats")
                        .queryParam("id", String.valueOf(parkingId))
                        .queryParam("start_timestamp", start.toString())
                        .queryParam("end_timestamp", end.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsage").value(stats.totalUsage()))
                .andExpect(jsonPath("$.averageAvailability").value(stats.averageAvailability()))
                .andExpect(jsonPath("$.peakOccupancyAt")
                        .value(DateTimeFormatter.ISO_DATE_TIME.format(stats.peakOccupancyAt())));
    }

    @Test
    void getParkingStats_withIncorrectDateTimeRangeAndNoParkingId_returnNullParkingStats() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 0, 0);
        when(parkingService.getParkingStats(null, start, end))
                .thenReturn(Result.success(new ParkingStatsResponse(0L, 0.0, null)));

        mockMvc.perform(get("/parkings/stats")
                        .queryParam("start_timestamp", start.toString())
                        .queryParam("end_timestamp", end.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsage").value(0L))
                .andExpect(jsonPath("$.averageAvailability").value(0.0))
                .andExpect(jsonPath("$.peakOccupancyAt").doesNotExist());
    }

    @Test
    void getParkingStats_withValidDateRangeAndNoParkingId_returnParkingStatsWithinRange() throws Exception {
        LocalDate start = LocalDate.of(2024, 12, 7);
        LocalDate end = LocalDate.of(2025, 1, 19);
        ParkingStatsResponse stats = new ParkingStatsResponse(21L, 0.35, LocalDateTime.now());
        when(parkingService.getParkingStats(null, start.atStartOfDay(), end.atTime(23, 59, 59)))
                .thenReturn(Result.success(stats));

        mockMvc.perform(get("/parkings/stats/date")
                        .queryParam("start_date", start.toString())
                        .queryParam("end_date", end.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsage").value(stats.totalUsage()))
                .andExpect(jsonPath("$.averageAvailability").value(stats.averageAvailability()))
                .andExpect(jsonPath("$.peakOccupancyAt")
                        .value(DateTimeFormatter.ISO_DATE_TIME.format(stats.peakOccupancyAt())));
    }

    @Test
    void getParkingStats_withValidParkingIdAndTimeRange_returnParkingStatsForParkingWithinRange() throws Exception {
        int parkingId = 1;
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(18, 0);
        ParkingStatsResponse stats = new ParkingStatsResponse(21L, 0.35, LocalDateTime.now());
        when(parkingService.getParkingStats(parkingId, start, end)).thenReturn(Result.success(stats));

        mockMvc.perform(get("/parkings/stats/time")
                        .queryParam("id", String.valueOf(parkingId))
                        .queryParam("start_time", start.toString())
                        .queryParam("end_time", end.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsage").value(stats.totalUsage()))
                .andExpect(jsonPath("$.averageAvailability").value(stats.averageAvailability()))
                .andExpect(jsonPath("$.peakOccupancyAt")
                        .value(DateTimeFormatter.ISO_DATE_TIME.format(stats.peakOccupancyAt())));
    }

    @Test
    void getParkingStats_withValidParkingIdAndStartTimeRange_returnParkingStatsForParkingFromStartTime()
            throws Exception {
        int parkingId = 1;
        LocalTime start = LocalTime.of(8, 0);
        ParkingStatsResponse stats = new ParkingStatsResponse(21L, 0.35, LocalDateTime.now());
        when(parkingService.getParkingStats(parkingId, start, null)).thenReturn(Result.success(stats));

        mockMvc.perform(get("/parkings/stats/time")
                        .queryParam("id", String.valueOf(parkingId))
                        .queryParam("start_time", start.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsage").value(stats.totalUsage()))
                .andExpect(jsonPath("$.averageAvailability").value(stats.averageAvailability()))
                .andExpect(jsonPath("$.peakOccupancyAt")
                        .value(DateTimeFormatter.ISO_DATE_TIME.format(stats.peakOccupancyAt())));
    }

    @Test
    void getParkingStats_withIncorrectParkingIdAndTimeRange_returnNotFound() throws Exception {
        int parkingId = -1;
        LocalTime start = LocalTime.of(18, 0);
        LocalTime end = LocalTime.of(8, 0);
        when(parkingService.getParkingStats(parkingId, start, end))
                .thenReturn(Result.failure(new ParkingError.ParkingNotFoundById(parkingId)));

        mockMvc.perform(get("/parkings/stats/time")
                        .queryParam("id", String.valueOf(parkingId))
                        .queryParam("start_time", start.toString())
                        .queryParam("end_time", end.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }
}
