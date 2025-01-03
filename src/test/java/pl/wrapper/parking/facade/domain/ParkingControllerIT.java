package pl.wrapper.parking.facade.domain;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import pl.wrapper.parking.facade.dto.NominatimLocation;
import pl.wrapper.parking.infrastructure.inMemory.ParkingDataRepository;
import pl.wrapper.parking.infrastructure.inMemory.dto.ParkingData;
import pl.wrapper.parking.infrastructure.nominatim.client.NominatimClient;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Flux;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ParkingControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PwrApiServerCaller pwrApiServerCaller;

    @MockBean
    private NominatimClient nominatimClient;

    @MockBean
    private ParkingDataRepository dataRepository;

    private List<ParkingResponse> parkings;
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
    void getClosestParking_returnClosestParking() throws Exception {
        String address = "test place";
        NominatimLocation location = new NominatimLocation(37.0, -158.0);

        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.just(location));
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);

        mockMvc.perform(get("/parkings/address").queryParam("address", address).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parkingId", is(1)))
                .andExpect(jsonPath("$.name", is("Parking 1")))
                .andExpect(jsonPath("$.address.geoLatitude").value(37.1f))
                .andExpect(jsonPath("$.address.geoLongitude").value(-158.8f));
    }

    @Test
    void getClosestParking_returnNotFound_whenNoResultsFromApi() throws Exception {
        String address = "non-existent address";
        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.empty());

        mockMvc.perform(get("/parkings/address").queryParam("address", address).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", anything()));

        verify(pwrApiServerCaller, never()).fetchData();
    }

    @Test
    void getClosestParking_returnNotFound_whenNoParkingsAvailable() throws Exception {
        String address = "test place";
        NominatimLocation location = new NominatimLocation(37.0, -158.0);
        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.just(location));
        when(pwrApiServerCaller.fetchData()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/parkings/address").queryParam("address", address).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }

    @Test
    public void getParkingByParams_returnAllParkings_whenNoParamsGiven() throws Exception {
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);

        mockMvc.perform(get("/parkings").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(parkings)));
    }

    @Test
    public void getParkingBySymbol_returnFoundParking() throws Exception {
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);

        mockMvc.perform(get("/parkings/symbol")
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("symbol", "P1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parkingId", is(1)))
                .andExpect(jsonPath("$.name", is("Parking 1")))
                .andExpect(jsonPath("$.address.geoLatitude").value(37.1f))
                .andExpect(jsonPath("$.address.geoLongitude").value(-158.8f));
    }

    @Test
    public void getParkingByName_returnNoParkings() throws Exception {
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);

        mockMvc.perform(get("/parkings/name")
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("name", "Non-existent name"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }

    @Test
    void getParkingStats_withValidParkingIdAndDateTimeRange_returnParkingStatsForParkingWithinRange() throws Exception {
        int parkingId = 1;
        LocalDateTime start = LocalDateTime.of(2024, 12, 7, 15, 45);
        LocalDateTime end = LocalDateTime.of(2025, 1, 19, 4, 0);
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);
        when(dataRepository.values()).thenReturn(dataList);

        mockMvc.perform(get("/parkings/stats")
                        .queryParam("id", String.valueOf(parkingId))
                        .queryParam("start_timestamp", start.toString())
                        .queryParam("end_timestamp", end.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsage").value(21L))
                .andExpect(jsonPath("$.averageAvailability").value(0.35))
                .andExpect(jsonPath("$.peakOccupancyAt")
                        .value(DateTimeFormatter.ISO_DATE_TIME.format(
                                dataList.get(1).timestamp())));
    }

    @Test
    void getParkingStats_withIncorrectDateTimeRangeAndNoParkingId_returnNullParkingStats() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 0, 0);
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);
        when(dataRepository.values()).thenReturn(dataList);

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
        LocalDate start = LocalDate.of(2024, 12, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);
        when(dataRepository.values()).thenReturn(dataList);

        mockMvc.perform(get("/parkings/stats/date")
                        .queryParam("start_date", start.toString())
                        .queryParam("end_date", end.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsage").value(15L))
                .andExpect(jsonPath("$.averageAvailability", Matchers.closeTo(0.58, 0.01)))
                .andExpect(jsonPath("$.peakOccupancyAt")
                        .value(DateTimeFormatter.ISO_DATE_TIME.format(
                                dataList.getFirst().timestamp())));
    }

    @Test
    void getParkingStats_withValidParkingIdAndTimeRange_returnParkingStatsForParkingWithinRange() throws Exception {
        int parkingId = 1;
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(18, 0);
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);
        when(dataRepository.values()).thenReturn(dataList);

        mockMvc.perform(get("/parkings/stats/time")
                        .queryParam("id", String.valueOf(parkingId))
                        .queryParam("start_time", start.toString())
                        .queryParam("end_time", end.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsage").value(5L))
                .andExpect(jsonPath("$.averageAvailability").value(0.5))
                .andExpect(jsonPath("$.peakOccupancyAt")
                        .value(DateTimeFormatter.ISO_DATE_TIME.format(
                                dataList.getFirst().timestamp())));
    }

    @Test
    void getParkingStats_withValidParkingIdAndEndTimeRange_returnParkingStatsForParkingUpToEndTime() throws Exception {
        int parkingId = 1;
        LocalTime end = LocalTime.of(18, 0);
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);
        when(dataRepository.values()).thenReturn(dataList);

        mockMvc.perform(get("/parkings/stats/time")
                        .queryParam("id", String.valueOf(parkingId))
                        .queryParam("end_time", end.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsage").value(5L))
                .andExpect(jsonPath("$.averageAvailability").value(0.5))
                .andExpect(jsonPath("$.peakOccupancyAt")
                        .value(DateTimeFormatter.ISO_DATE_TIME.format(
                                dataList.getFirst().timestamp())));
    }

    @Test
    void getParkingStats_withIncorrectParkingIdAndTimeRange_returnNotFound() throws Exception {
        int parkingId = -1;
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(18, 0);
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);

        mockMvc.perform(get("/parkings/stats/time")
                        .queryParam("id", String.valueOf(parkingId))
                        .queryParam("start_time", start.toString())
                        .queryParam("end_time", end.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", anything()));

        verify(dataRepository, never()).values();
    }
}
