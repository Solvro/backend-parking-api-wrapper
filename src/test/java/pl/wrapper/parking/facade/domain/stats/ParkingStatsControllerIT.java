package pl.wrapper.parking.facade.domain.stats;

import static java.time.DayOfWeek.*;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.wrapper.parking.infrastructure.inMemory.ParkingDataRepository;
import pl.wrapper.parking.infrastructure.inMemory.dto.AvailabilityData;
import pl.wrapper.parking.infrastructure.inMemory.dto.ParkingData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ParkingStatsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingDataRepository dataRepository;

    private List<ParkingData> parkingData;

    @BeforeEach
    public void setUp() {
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
        mockMvc.perform(get("/stats").accept(MediaType.APPLICATION_JSON).queryParam("time", "incorrect"))
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
                        .queryParam("ids", "1"))
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
