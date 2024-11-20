package pl.wrapper.parking.exception;

import org.junit.jupiter.api.Test;
import pl.wrapper.parking.result.FailureResult;
import pl.wrapper.parking.result.Result;
import pl.wrapper.parking.result.SuccessResult;
import static org.junit.jupiter.api.Assertions.*;


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