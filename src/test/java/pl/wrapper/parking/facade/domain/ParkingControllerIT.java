package pl.wrapper.parking.facade.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pl.wrapper.parking.facade.client.NominatimClient;
import pl.wrapper.parking.facade.dto.NominatimLocation;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private List<ParkingResponse> parkings;

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
                        .build()
        );
    }

    @Test
    void getAllParkings_returnParkingList() throws Exception {
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/parkings")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(parkings)));
    }

    @Test
    void getClosestParking_returnClosestParking() throws Exception {
        String address = "test place";
        NominatimLocation location = new NominatimLocation(37.0, -158.0);

        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.just(location));
        when(pwrApiServerCaller.fetchData()).thenReturn(parkings);

        mockMvc.perform(get("/v1/parkings")
                        .queryParam("address", address)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parkingId", is(1)))
                .andExpect(jsonPath("$.name", is("Parking 1")))
                .andExpect(jsonPath("$.address.geoLatitude").value(37.1f))
                .andExpect(jsonPath("$.address.geoLongitude").value(-158.8f));
    }

    @Test
    void getClosestParking_returnNotFound() throws Exception {
        String address = "non-existent address";
        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.empty());

        mockMvc.perform(get("/v1/parkings")
                        .queryParam("address", address)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", anything()));

        verify(pwrApiServerCaller, never()).fetchData();
    }

    @Test
    void getClosestParking_returnInternalServerError(@Value("${general.exception.message}") String errorMessage) throws Exception {
        String address = "test place";
        NominatimLocation location = new NominatimLocation(37.0, -158.0);
        when(nominatimClient.search(eq(address), anyString())).thenReturn(Flux.just(location));
        when(pwrApiServerCaller.fetchData()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/parkings")
                        .queryParam("address", address)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorMessage", is(errorMessage)));
    }
}
