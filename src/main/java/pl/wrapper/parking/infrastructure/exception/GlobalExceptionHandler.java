package pl.wrapper.parking.infrastructure.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.wrapper.parking.infrastructure.error.ErrorWrapper;

@ControllerAdvice
@RequiredArgsConstructor
class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorWrapper> handleGeneralException(Exception e, HttpServletRequest request) {
        ErrorWrapper errorWrapper =
                new ErrorWrapper("An error has occured", HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURI());
        return new ResponseEntity<>(errorWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidCallException.class)
    public ResponseEntity<ErrorWrapper> handleInvalidCallException(InvalidCallException e, HttpServletRequest request) {
        ErrorWrapper errorWrapper = new ErrorWrapper("Invalid call", HttpStatus.BAD_REQUEST, request.getRequestURI());
        return new ResponseEntity<>(errorWrapper, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PwrApiNotRespondingException.class)
    public ResponseEntity<ErrorWrapper> handlePwrApiNotRespondingException(
            PwrApiNotRespondingException e, HttpServletRequest request) {
        ErrorWrapper errorWrapper =
                new ErrorWrapper("PWR Api not responding", HttpStatus.SERVICE_UNAVAILABLE, request.getRequestURI());
        return new ResponseEntity<>(errorWrapper, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorWrapper> handleJsonProcessingException(
            JsonProcessingException e, HttpServletRequest request) {
        ErrorWrapper errorWrapper =
                new ErrorWrapper("Json processing error", HttpStatus.BAD_REQUEST, request.getRequestURI());
        return new ResponseEntity<>(errorWrapper, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorWrapper> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        ErrorWrapper errorWrapper =
                new ErrorWrapper("Null pointer exception", HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURI());
        return new ResponseEntity<>(errorWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ClassCastException.class)
    public ResponseEntity<ErrorWrapper> handleClassCastException(ClassCastException e, HttpServletRequest request) {
        ErrorWrapper errorWrapper =
                new ErrorWrapper("Class cast exception", HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURI());
        return new ResponseEntity<>(errorWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
