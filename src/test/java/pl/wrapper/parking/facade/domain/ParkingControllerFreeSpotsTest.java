package pl.wrapper.parking.facade.domain;

import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.infrastructure.error.ParkingError;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.anything;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class ParkingControllerFreeSpotsTest {

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
                        .build()
        );
    }

    @Test()
    void getAllParkingsWithFreeSpots_shouldReturnListOfParking() throws Exception {
        List<ParkingResponse> serviceResponse = List.of(parkingData.get(1), parkingData.get(2), parkingData.get(3));
        when(parkingService.getAllWithFreeSpots(null)).thenReturn(serviceResponse);

        String jsonResponse = mockMvc.perform(get("/parkings/all-with-free-spots")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

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

        String jsonResponse = mockMvc.perform(get("/parkings/all-with-free-spots")
                        .queryParam("opened", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

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

        String jsonResponse = mockMvc.perform(get("/parkings/all-with-free-spots")
                        .queryParam("opened", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        JSONArray jsonArray = new JSONArray(jsonResponse);
        assertEquals(0, jsonArray.length());
    }

    @Test
    void getClosedParkingsWithFreeSpots_shouldReturnListOfParking() throws Exception {
        List<ParkingResponse> serviceResponse = List.of(parkingData.get(1), parkingData.get(2));
        when(parkingService.getAllWithFreeSpots(false)).thenReturn(serviceResponse);

        String jsonResponse = mockMvc.perform(get("/parkings/all-with-free-spots")
                        .queryParam("opened", "false")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

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

        mockMvc.perform(get("/parkings/most-free-spots")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeSpots").value(325))
                .andExpect(jsonPath("$.symbol").value("P2"));
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromOpened_shouldReturnParking() throws Exception {
        Result<ParkingResponse> serviceResponse = Result.success(parkingData.get(3));
        when(parkingService.getWithTheMostFreeSpots(true)).thenReturn(serviceResponse);

        mockMvc.perform(get("/parkings/most-free-spots")
                        .queryParam("opened", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeSpots").value(51))
                .andExpect(jsonPath("$.symbol").value("P4"));
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromClosed_shouldReturnParking() throws Exception {
        Result<ParkingResponse> serviceResponse = Result.success(parkingData.get(1));
        when(parkingService.getWithTheMostFreeSpots(false)).thenReturn(serviceResponse);

        mockMvc.perform(get("/parkings/most-free-spots")
                        .queryParam("opened", "false")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeSpots").value(325))
                .andExpect(jsonPath("$.symbol").value("P2"));
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromClosed_shouldReturnNotFound() throws Exception {
        Result<ParkingResponse> serviceResponse = Result.failure(new ParkingError.ParkingWithTheMostFreeSpotsNotFound());
        when(parkingService.getWithTheMostFreeSpots(false)).thenReturn(serviceResponse);

        mockMvc.perform(get("/parkings/most-free-spots")
                        .queryParam("opened", "false")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }
}
