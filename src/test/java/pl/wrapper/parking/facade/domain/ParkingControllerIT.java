package pl.wrapper.parking.facade.domain;

import static java.time.DayOfWeek.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import pl.wrapper.parking.infrastructure.inMemory.dto.AvailabilityData;
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
    private List<ParkingData> parkingData;

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
                ParkingData.builder()
                        .parkingId(1)
                        .totalSpots(100)
                        .freeSpotsHistory(Map.of(
                                MONDAY,
                                Map.of(
                                        LocalTime.of(10, 0), new AvailabilityData(1, 0.8),
                                        LocalTime.of(12, 0), new AvailabilityData(1, 0.5)),
                                TUESDAY,
                                Map.of(LocalTime.of(10, 0), new AvailabilityData(1, 0.7))))
                        .build(),
                ParkingData.builder()
                        .parkingId(2)
                        .totalSpots(200)
                        .freeSpotsHistory(Map.of(
                                MONDAY, Map.of(LocalTime.of(10, 0), new AvailabilityData(1, 0.6)),
                                WEDNESDAY, Map.of(LocalTime.of(14, 0), new AvailabilityData(1, 0.9))))
                        .build());
    }

    @Test
    void getClosestParking_returnClosestParking() throws Exception {
        String address = "test place";
        NominatimLocation location = new NominatimLocation(37.0, -158.0);

        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.just(location));
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);

        mockMvc.perform(get("/address").queryParam("address", address).accept(MediaType.APPLICATION_JSON))
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

        mockMvc.perform(get("/address").queryParam("address", address).accept(MediaType.APPLICATION_JSON))
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

        mockMvc.perform(get("/address").queryParam("address", address).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }

    @Test
    public void getParkingByParams_returnAllParkings_whenNoParamsGiven() throws Exception {
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);

        mockMvc.perform(get("").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(parkings)));
    }

    @Test
    public void getParkingBySymbol_returnFoundParking() throws Exception {
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);

        mockMvc.perform(get("/symbol")
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

        mockMvc.perform(get("/name")
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("name", "Non-existent name"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }

    @Test
    void getParkingStats_withDayOfWeekAndTime_returnCorrectStats() throws Exception {
        when(dataRepository.values()).thenReturn(parkingData);

        mockMvc.perform(get("/stats")
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("day_of_week", "MONDAY")
                        .queryParam("time", "10:07:15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].parkingInfo.parkingId", is(1)))
                .andExpect(jsonPath("$[0].stats.averageAvailability", is(0.8)))
                .andExpect(jsonPath("$[1].parkingInfo.parkingId", is(2)))
                .andExpect(jsonPath("$[1].stats.averageAvailability", is(0.6)));
    }

    @Test
    void getParkingStats_withWeirdIdListAndTime_returnCorrectStats() throws Exception {
        when(dataRepository.fetchAllKeys()).thenReturn(Set.of(1, 2));
        when(dataRepository.get(anyInt())).thenReturn(parkingData.get(0), parkingData.get(1));

        mockMvc.perform(get("/stats")
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("time", "10:07:15")
                        .queryParam("ids", "1", "2", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].parkingInfo.parkingId", is(1)))
                .andExpect(jsonPath("$[0].stats.averageAvailability", is(0.75)))
                .andExpect(jsonPath("$[1].parkingInfo.parkingId", is(2)))
                .andExpect(jsonPath("$[1].stats.averageAvailability", is(0.6)));

        verify(dataRepository, never()).values();
    }

    @Test
    void getParkingStats_withEmptyDataRepository_returnEmptyList() throws Exception {
        when(dataRepository.values()).thenReturn(List.of());

        mockMvc.perform(get("/stats")
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("time", "10:07:15")
                        .queryParam("ids", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void getParkingStats_withIncorrectTimeFormat_returnBadRequest() throws Exception {
        mockMvc.perform(get("/stats")
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("time", "incorrect"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }

    @Test
    void getDailyParkingStats_withIdList_returnCorrectDailyStats() throws Exception {
        when(dataRepository.fetchAllKeys()).thenReturn(Set.of(1, 2));
        when(dataRepository.get(anyInt())).thenReturn(parkingData.get(0), parkingData.get(1));

        mockMvc.perform(get("/stats/daily")
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("day_of_week", "MONDAY")
                        .queryParam("ids", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].parkingInfo.parkingId", is(1)))
                .andExpect(jsonPath("$[0].stats.averageAvailability", is(0.65)))
                .andExpect(jsonPath("$[0].maxOccupancyAt", is("12:00:00")))
                .andExpect(jsonPath("$[0].minOccupancyAt", is("10:00:00")));

        verify(dataRepository, never()).values();
    }

    @Test
    void getDailyParkingStats_withMissingDayOfWeek_returnBadRequest() throws Exception {
        mockMvc.perform(get("/stats/daily"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }

    @Test
    void getWeeklyParkingStats_withEmptyIdList_returnCorrectWeeklyStats() throws Exception {
        when(dataRepository.values()).thenReturn(parkingData);

        mockMvc.perform(get("/stats/weekly").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].parkingInfo.parkingId", is(1)))
                .andExpect(jsonPath("$[0].stats.averageAvailability", Matchers.closeTo(0.666, 0.0011)))
                .andExpect(jsonPath("$[0].maxOccupancyInfo.dayOfWeek", is("MONDAY")))
                .andExpect(jsonPath("$[0].maxOccupancyInfo.time", is("12:00:00")))
                .andExpect(jsonPath("$[0].minOccupancyInfo.dayOfWeek", is("MONDAY")))
                .andExpect(jsonPath("$[0].minOccupancyInfo.time", is("10:00:00")))
                .andExpect(jsonPath("$[1].parkingInfo.parkingId", is(2)))
                .andExpect(jsonPath("$[1].stats.averageAvailability", is(0.75)))
                .andExpect(jsonPath("$[1].maxOccupancyInfo.dayOfWeek", is("MONDAY")))
                .andExpect(jsonPath("$[1].maxOccupancyInfo.time", is("10:00:00")))
                .andExpect(jsonPath("$[1].minOccupancyInfo.dayOfWeek", is("WEDNESDAY")))
                .andExpect(jsonPath("$[1].minOccupancyInfo.time", is("14:00:00")));
    }

    @Test
    void getCollectiveDailyParkingStats_withWeirdIdList_returnCorrectCollectiveDailyStats() throws Exception {
        when(dataRepository.fetchAllKeys()).thenReturn(Set.of(1, 2));
        when(dataRepository.get(1)).thenReturn(parkingData.getFirst());

        mockMvc.perform(get("/stats/daily/collective")
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("day_of_week", "MONDAY")
                        .queryParam("ids", "-7", "1", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].parkingInfo.parkingId", is(1)))
                .andExpect(jsonPath("$[0].statsMap", Matchers.aMapWithSize(2)))
                .andExpect(jsonPath("$[0].statsMap['10:00'].averageAvailability", is(0.8)))
                .andExpect(jsonPath("$[0].statsMap['12:00'].averageAvailability", is(0.5)));

        verify(dataRepository, never()).values();
    }

    @Test
    void getCollectiveDailyParkingStats_withMissingDayOfWeek_returnBadRequest() throws Exception {
        mockMvc.perform(get("/stats/daily/collective"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }

    @Test
    void getCollectiveWeeklyParkingStats_withoutIdList_returnCorrectCollectiveWeeklyStats() throws Exception {
        when(dataRepository.values()).thenReturn(parkingData);

        mockMvc.perform(get("/stats/weekly/collective"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].parkingInfo.parkingId", is(1)))
                .andExpect(jsonPath("$[0].statsMap", Matchers.aMapWithSize(2)))
                .andExpect(jsonPath("$[0].statsMap['MONDAY']", Matchers.aMapWithSize(2)))
                .andExpect(jsonPath("$[0].statsMap['TUESDAY']", Matchers.aMapWithSize(1)))
                .andExpect(jsonPath("$[0].statsMap['MONDAY']['10:00'].averageAvailability", is(0.8)))
                .andExpect(jsonPath("$[0].statsMap['MONDAY']['12:00'].averageAvailability", is(0.5)))
                .andExpect(jsonPath("$[0].statsMap['TUESDAY']['10:00'].averageAvailability", is(0.7)))
                .andExpect(jsonPath("$[1].parkingInfo.parkingId", is(2)))
                .andExpect(jsonPath("$[1].statsMap", Matchers.aMapWithSize(2)))
                .andExpect(jsonPath("$[1].statsMap['MONDAY']", Matchers.aMapWithSize(1)))
                .andExpect(jsonPath("$[1].statsMap['WEDNESDAY']", Matchers.aMapWithSize(1)))
                .andExpect(jsonPath("$[1].statsMap['MONDAY']['10:00'].averageAvailability", is(0.6)))
                .andExpect(jsonPath("$[1].statsMap['WEDNESDAY']['14:00'].averageAvailability", is(0.9)));
    }
}
