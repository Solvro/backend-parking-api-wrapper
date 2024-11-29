package pl.wrapper.parking.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.wrapper.parking.facade.domain.DummyController;
import pl.wrapper.parking.facade.domain.DummyService;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.domain.PwrApiCaller;
import pl.wrapper.parking.pwrResponseHandler.domain.PwrApiServerCallerImpl;
import reactor.core.publisher.Mono;

@WebMvcTest(DummyController.class)
@Import({DummyService.class})
class ResultTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnDummyBody() throws Exception {
        Long id = 4L;
        MvcResult mvcResult = mockMvc.perform(get("/id/{id}", id)) // add url and variables
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(); // get response body

        Integer status = mvcResult.getResponse().getStatus(); // get response status

        Integer OkStatus = 200;
        assertEquals(status, OkStatus); // check status
        assertEquals(responseBody, String.valueOf(id)); // check response body
    }

    @Test
    void shouldReturnException() {
        Exception provided = new ClassCastException("simulated");

        PwrApiCaller apiCaller = Mockito.mock(PwrApiCaller.class);
        Mockito.when(apiCaller.fetchParkingPlaces()).thenReturn(Mono.error(provided)); // simulate response

        PwrApiServerCaller pwrApiServerCaller = new PwrApiServerCallerImpl(apiCaller);
        Exception e = assertThrows(provided.getClass(), pwrApiServerCaller::fetchData); // check error class

        assertEquals(provided.getMessage(), e.getMessage()); // check error message
    }
}
