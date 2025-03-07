package pl.wrapper.parking.facade.domain.stats.request;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.wrapper.parking.facade.ParkingRequestStatsService;
import pl.wrapper.parking.facade.dto.stats.request.EndpointStats;

@WebMvcTest(ParkingRequestStatsController.class)
@ComponentScan({
    "pl.wrapper.parking.infrastructure",
    "pl.wrapper.parking.facade.main",
    "pl.wrapper.parking.pwrResponseHandler.domain"
})
public class ParkingRequestStatsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingRequestStatsService parkingRequestStatsService;

    @Test
    void getBasicRequestStats_shouldReturnData() throws Exception {
        Map<String, EndpointStats> stats = Map.of(
                "parkings/free", new EndpointStats(3, 2, 0.67),
                "parkings", new EndpointStats(5, 3, 0.6));

        when(parkingRequestStatsService.getBasicRequestStats()).thenReturn(stats);

        mockMvc.perform(get("/stats/requests").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(stats)));
    }

    @Test
    void getBasicRequestStats_shouldReturnEmptyData() throws Exception {
        when(parkingRequestStatsService.getBasicRequestStats()).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/stats/requests").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    void getRequestStatsForTimes_shouldReturnData() throws Exception {
        Map<String, List<Map.Entry<String, Double>>> timeStats = Map.of(
                "parkings/free", List.of(Map.entry("00:00 - 00:30", 0.4), Map.entry("00:30 - 01:00", 0.32)),
                "parkings", List.of(Map.entry("00:00 - 00:30", 0.31), Map.entry("00:30 - 01:00", 0.26)));

        when(parkingRequestStatsService.getRequestStatsForTimes()).thenReturn(timeStats);

        mockMvc.perform(get("/stats/requests/times").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(timeStats)));
    }

    @Test
    void getRequestStatsForTimes_shouldReturnEmptyData() throws Exception {
        when(parkingRequestStatsService.getRequestStatsForTimes()).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/stats/requests/times").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    void getRequestPeakTimes_shouldReturnData() throws Exception {
        List<Map.Entry<String, Double>> peakTimesStats = List.of(
                Map.entry("13:00 - 13:30", 23.0), Map.entry("18:30 - 19:00", 21.2), Map.entry("9:00 - 9:30", 18.4));

        when(parkingRequestStatsService.getRequestPeakTimes()).thenReturn(peakTimesStats);

        mockMvc.perform(get("/stats/requests/peak-times").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(peakTimesStats)));
    }

    @Test
    void getRequestPeakTimes_shouldReturnEmptyData() throws Exception {
        when(parkingRequestStatsService.getRequestPeakTimes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/stats/requests/peak-times").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getDailyRequestStats_shouldReturnData() throws Exception {
        Map<String, Double> dailyStats = Map.of(
                "parkings/free", 2.8,
                "parkings/free/top", 2.5,
                "parkings", 4.0);

        when(parkingRequestStatsService.getDailyRequestStats()).thenReturn(dailyStats);

        mockMvc.perform(get("/stats/requests/day").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(dailyStats)));
    }

    @Test
    void getDailyRequestStats_shouldReturnEmptyData() throws Exception {
        when(parkingRequestStatsService.getDailyRequestStats()).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/stats/requests/day").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    void getDailyRequestStats_missingData_shouldReturnData() throws Exception {
        Map<String, Double> dailyStats = new HashMap<>();
        dailyStats.put("parkings/free", 2.8);
        dailyStats.put("parkings/free/top", null);

        when(parkingRequestStatsService.getDailyRequestStats()).thenReturn(dailyStats);

        mockMvc.perform(get("/stats/requests/day").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(dailyStats)));
    }
}
