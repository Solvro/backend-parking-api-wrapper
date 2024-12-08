package pl.wrapper.parking.infrastructure.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.wrapper.parking.facade.exception.AddressNotFoundException;
import pl.wrapper.parking.infrastructure.error.ErrorWrapper;

import java.util.NoSuchElementException;

@ControllerAdvice
@RequiredArgsConstructor
class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorWrapper> handleGeneralException(HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorWrapper errorWrapper =
                new ErrorWrapper("An error has occurred", status, request.getRequestURI(), status);
        return new ResponseEntity<>(errorWrapper, status);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorWrapper> handleNoSuchElementException(NoSuchElementException ex, ServerHttpRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorWrapper errorWrapper =
                new ErrorWrapper(ex.getMessage(), status, request.getURI().toString(), status);
        return new ResponseEntity<>(errorWrapper, status);
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ErrorWrapper> handleAddressNotFoundException(AddressNotFoundException ex, ServerHttpRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorWrapper errorWrapper =
                new ErrorWrapper(ex.getMessage(), status, request.getURI().toString(), status);
        return new ResponseEntity<>(errorWrapper, status);
    }

    @ExceptionHandler(InvalidCallException.class)
    public ResponseEntity<ErrorWrapper> handleInvalidCallException(HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorWrapper errorWrapper = new ErrorWrapper("Invalid call", status, request.getRequestURI(), status);
        return new ResponseEntity<>(errorWrapper, status);
    }

    @ExceptionHandler(PwrApiNotRespondingException.class)
    public ResponseEntity<ErrorWrapper> handlePwrApiNotRespondingException(HttpServletRequest request) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        ErrorWrapper errorWrapper =
                new ErrorWrapper("PWR Api not responding", status, request.getRequestURI(), status);
        return new ResponseEntity<>(errorWrapper, status);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorWrapper> handleJsonProcessingException(HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorWrapper errorWrapper =
                new ErrorWrapper("Json processing error", status, request.getRequestURI(), status);
        return new ResponseEntity<>(errorWrapper, status);
    }

    @ExceptionHandler(ClassCastException.class)
    public ResponseEntity<ErrorWrapper> handleClassCastException(HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorWrapper errorWrapper =
                new ErrorWrapper("Class cast exception", status, request.getRequestURI(), status);
        return new ResponseEntity<>(errorWrapper, status);
    }
}
