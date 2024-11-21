package pl.wrapper.parking.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ExceptionStatus {
    GENERAL(Exception.class,HttpStatus.INTERNAL_SERVER_ERROR),
    ILLEGAL_ARGUMENT(IllegalArgumentException.class, HttpStatus.BAD_REQUEST),
    NULL_POINTER(NullPointerException.class, HttpStatus.INTERNAL_SERVER_ERROR),
    INDEX_OUT_OF_BOUNDS(IndexOutOfBoundsException.class, HttpStatus.BAD_REQUEST);

    private final Class<? extends Exception> exceptionClass;
    private final HttpStatus httpStatus;

    public static HttpStatus getStatusForException(Class<? extends Exception> exceptionClass) {
        for (ExceptionStatus exceptionStatus : values())
            if (exceptionStatus.getExceptionClass().equals(exceptionClass))
                return exceptionStatus.getHttpStatus();

        return GENERAL.getHttpStatus();
    }
}
