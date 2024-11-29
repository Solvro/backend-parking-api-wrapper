package pl.wrapper.parking.exception;


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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


class ResultTest {

    @Test
    void valid() {
        String success = "success!";
        Result<String> result = new SuccessResult<>(success);

        assertEquals(result.getValue(), success);

        assertTrue(result.isSuccess());
    }

    @Test
    void invalid() {
        IllegalArgumentException exception = new IllegalArgumentException();

        Result<Integer> result1 = new FailureResult<>(exception);
        Result<String> result2 = new FailureResult<>(exception);

        assertThrowsExactly(IllegalArgumentException.class, result1::getValue);
        assertThrowsExactly(IllegalArgumentException.class, result2::getValue);

        assertFalse(result1.isSuccess());
        assertFalse(result2.isSuccess());
    }
}