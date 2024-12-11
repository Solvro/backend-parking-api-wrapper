package pl.wrapper.parking.infrastructure.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.wrapper.parking.facade.exception.AddressNotFoundException;
import pl.wrapper.parking.infrastructure.error.ErrorWrapper;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
class GlobalExceptionHandler {

    @Value("${general.exception.message}")
    private String generalErrorMessage;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorWrapper> handleGeneralException(Exception e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorWrapper errorWrapper = new ErrorWrapper(generalErrorMessage, status, request.getRequestURI(), status);
        logError(generalErrorMessage, request.getRequestURI(), e);
        return new ResponseEntity<>(errorWrapper, status);
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ErrorWrapper> handleAddressNotFoundException(AddressNotFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        String message = ex.getMessage();
        ErrorWrapper errorWrapper = new ErrorWrapper(message, status, request.getRequestURI(), status);
        logError(message, request.getRequestURI(), ex);
        return new ResponseEntity<>(errorWrapper, status);
    }

    @ExceptionHandler(InvalidCallException.class)
    public ResponseEntity<ErrorWrapper> handleInvalidCallException(InvalidCallException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Invalid call";
        ErrorWrapper errorWrapper = new ErrorWrapper(message, status, request.getRequestURI(), status);
        logError(message, request.getRequestURI(), e);
        return new ResponseEntity<>(errorWrapper, status);
    }

    @ExceptionHandler(PwrApiNotRespondingException.class)
    public ResponseEntity<ErrorWrapper> handlePwrApiNotRespondingException(PwrApiNotRespondingException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        String message = "PWR Api not responding";
        ErrorWrapper errorWrapper = new ErrorWrapper(message, status, request.getRequestURI(), status);
        logError(message, request.getRequestURI(), e);
        return new ResponseEntity<>(errorWrapper, status);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorWrapper> handleJsonProcessingException(JsonProcessingException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Json processing error";
        ErrorWrapper errorWrapper = new ErrorWrapper(message, status, request.getRequestURI(), status);
        logError(message, request.getRequestURI(), e);
        return new ResponseEntity<>(errorWrapper, status);
    }

    @ExceptionHandler(ClassCastException.class)
    public ResponseEntity<ErrorWrapper> handleClassCastException(ClassCastException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Class cast exception";
        ErrorWrapper errorWrapper = new ErrorWrapper(message, status, request.getRequestURI(), status);
        logError(message, request.getRequestURI(), e);
        return new ResponseEntity<>(errorWrapper, status);
    }

    private <T extends Exception> void logError(String message, String uri, T e){
        log.error("{}at uri: {}; Details: {}", message, uri, e.getMessage());
    }
}
