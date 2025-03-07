package pl.wrapper.parking.facade.domain.main;

import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.infrastructure.error.ParkingError;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

@WebMvcTest(ParkingController.class)
@ComponentScan({"pl.wrapper.parking.infrastructure", "pl.wrapper.parking.facade.main"})
public class ParkingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingService parkingService;

    private List<ParkingResponse> parkingData;

    @MockBean
    private PwrApiServerCaller pwrApiServerCaller;

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

        mockMvc.perform(get("/address").queryParam("address", address).accept(MediaType.APPLICATION_JSON))
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

        mockMvc.perform(get("/address").queryParam("address", address).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }

    @Test()
    void getAllParkingsWithFreeSpots_shouldReturnListOfParking() throws Exception {
        List<ParkingResponse> serviceResponse = List.of(parkingData.get(1), parkingData.get(2), parkingData.get(3));
        when(parkingService.getAllWithFreeSpots(null)).thenReturn(serviceResponse);

        String jsonResponse = mockMvc.perform(get("/free").accept(MediaType.APPLICATION_JSON))
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
                        get("/free").queryParam("opened", "true").accept(MediaType.APPLICATION_JSON))
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
                        get("/free").queryParam("opened", "true").accept(MediaType.APPLICATION_JSON))
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
                        get("/free").queryParam("opened", "false").accept(MediaType.APPLICATION_JSON))
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

        mockMvc.perform(get("/free/top").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeSpots").value(325))
                .andExpect(jsonPath("$.symbol").value("P2"));
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromOpened_shouldReturnParking() throws Exception {
        Result<ParkingResponse> serviceResponse = Result.success(parkingData.get(3));
        when(parkingService.getWithTheMostFreeSpots(true)).thenReturn(serviceResponse);

        mockMvc.perform(get("/free/top").queryParam("opened", "true").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeSpots").value(51))
                .andExpect(jsonPath("$.symbol").value("P4"));
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromClosed_shouldReturnParking() throws Exception {
        Result<ParkingResponse> serviceResponse = Result.success(parkingData.get(1));
        when(parkingService.getWithTheMostFreeSpots(false)).thenReturn(serviceResponse);

        mockMvc.perform(get("/free/top").queryParam("opened", "false").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeSpots").value(325))
                .andExpect(jsonPath("$.symbol").value("P2"));
    }

    @Test
    void getParkingWithTheMostFreeSpacesFromClosed_shouldReturnNotFound() throws Exception {
        Result<ParkingResponse> serviceResponse = Result.failure(new ParkingError.NoFreeParkingSpotsAvailable());
        when(parkingService.getWithTheMostFreeSpots(false)).thenReturn(serviceResponse);

        mockMvc.perform(get("/free/top").queryParam("opened", "false").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }

    @Test
    void getAllWithParams_shouldReturnResultBody() throws Exception {
        ParkingResponse parkingResponse = parkingData.getFirst();
        when(parkingService.getByParams(any(), any(), any(), any(), any())).thenReturn(List.of(parkingResponse));
        MvcResult result = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn();

        String listJson = result.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(listJson);
        JSONObject parking = jsonArray.getJSONObject(0);

        assertEquals(parking.getString("parkingId"), String.valueOf(parkingResponse.parkingId()));
    }

    @Test
    void getById_ShouldReturnError() throws Exception {
        int incorrectId = parkingData.getLast().parkingId() + 100;
        when(parkingService.getById(incorrectId, null))
                .thenReturn(Result.failure(new ParkingError.ParkingNotFoundById(incorrectId)));
        mockMvc.perform(get("/id").param("id", String.valueOf(incorrectId))).andExpect(status().isNotFound());
    }

    @Test
    void anyEndpoint_shouldParseCorrectly() throws Exception {
        ParkingResponse parkingResponse = parkingData.getFirst();
        Mockito.when(parkingService.getById(parkingResponse.parkingId(), null))
                .thenReturn(Result.success(parkingResponse));
        MvcResult mvcResult = mockMvc.perform(get("/id").param("id", String.valueOf(parkingResponse.parkingId())))
                .andReturn();
        Integer status = mvcResult.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status);
    }
}
