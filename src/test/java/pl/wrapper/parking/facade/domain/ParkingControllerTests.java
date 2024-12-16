package pl.wrapper.parking.facade.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.infrastructure.error.ParkingError;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParkingController.class)
public class ParkingControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingService parkingService;

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

        mockMvc.perform(get("/parkings/address")
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
        ParkingError.ParkingNotFoundByAddress error = new ParkingError.ParkingNotFoundByAddress(address);
        when(parkingService.getClosestParking(address)).thenReturn(Result.failure(error));

        mockMvc.perform(get("/parkings/address")
                        .queryParam("address", address)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", anything()));
    }
}
