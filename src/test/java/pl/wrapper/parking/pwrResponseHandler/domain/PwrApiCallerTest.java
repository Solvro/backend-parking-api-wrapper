package pl.wrapper.parking.pwrResponseHandler.domain;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PwrApiCallerTest {
    @Test
    void shouldReturnException() {
        Exception provided = new ClassCastException("simulated");

        PwrApiCaller apiCaller = Mockito.mock(PwrApiCaller.class);
        Mockito.when(apiCaller.fetchParkingPlaces()).thenReturn(Mono.error(provided));

        PwrApiServerCaller pwrApiServerCaller = new PwrApiServerCallerImpl(apiCaller);
        Exception e = assertThrows(provided.getClass(), pwrApiServerCaller::fetchParkingData);

        assertEquals(provided.getMessage(), e.getMessage());
    }
}
