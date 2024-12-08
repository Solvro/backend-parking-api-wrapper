package pl.wrapper.parking.facade.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

@WebMvcTest(ParkingController.class)
@ComponentScan(basePackageClasses = ParkingService.class)
public class ParkingServiceTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PwrApiServerCaller pwrApiServerCaller;

    private Integer correctId;

    @BeforeEach
    void setUp() {
        correctId = 1;
        String name = "parking";
        String symbol = "1";

        ParkingResponse parking = ParkingResponse.builder()
                .parkingId(correctId)
                .name(name)
                .symbol(symbol)
                .openingHours(LocalTime.now().minusHours(1L))
                .closingHours(LocalTime.now().plusHours(1L))
                .build();
        List<ParkingResponse> list = List.of(new ParkingResponse[]{parking});

        Mockito.when(pwrApiServerCaller.fetchData()).thenReturn(list);
    }

    @Test
    void shouldReturnResultBodyFromGeneralEndpoints() throws Exception {
        MvcResult result = mockMvc.perform(get("/id")
                        .param("id",String.valueOf(correctId)))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(json);

        assertEquals(jsonObject.getString("parkingId"), String.valueOf(correctId));
    }

    @Test
    void shouldReturnResultBodyFromGetWithAllParametersEndpoint() throws Exception {
        MvcResult result = mockMvc.perform(get(""))
                .andExpect(status().isOk())
                .andReturn();

        String listJson = result.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(listJson);
        JSONObject parking = jsonArray.getJSONObject(0);

        assertEquals(parking.getString("parkingId"), String.valueOf(correctId));
    }

    @Test
    void ShouldReturnError() throws Exception {
        mockMvc.perform(get("/id")
                .param("id", String.valueOf(correctId + 1)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/id")
                        .param("id", String.valueOf(correctId))
                        .param("opened","false"))
                .andExpect(status().isNotFound());
    }
}
