package pl.wrapper.parking.facade.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.facade.exception.AddressNotFoundException;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParkingController.class)
public class ParkingControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParkingService parkingService;

    @Test
    void getAllParkings_returnsParkingList() throws Exception {
        List<ParkingResponse> parkings = List.of(
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
                        .build()
        );
        when(parkingService.getAllParkings()).thenReturn(Result.success(parkings));

        mockMvc.perform(get("/v1/parkings")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(parkings)));
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

        mockMvc.perform(get("/v1/parkings")
                        .queryParam("address", address)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parkingId", is(parking.parkingId())))
                .andExpect(jsonPath("$.name", is(parking.name())))
                .andExpect(jsonPath("$.address.geoLatitude").value(parking.address().geoLatitude()))
                .andExpect(jsonPath("$.address.geoLongitude").value(parking.address().geoLongitude()));
    }

    @Test
    void getClosestParking_returnNotFound() throws Exception {
        String address = "non-existent address";
        AddressNotFoundException error = new AddressNotFoundException(address);
        when(parkingService.getClosestParking(address)).thenThrow(error);

        mockMvc.perform(get("/v1/parkings")
                        .queryParam("address", address)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }

    @Test
    void getClosestParking_returnInternalServerError() throws Exception {
        String address = "test place";
        NoSuchElementException error = new NoSuchElementException("No parkings available");
        when(parkingService.getClosestParking(address)).thenThrow(error);

        mockMvc.perform(get("/v1/parkings")
                        .queryParam("address", address)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }
}
