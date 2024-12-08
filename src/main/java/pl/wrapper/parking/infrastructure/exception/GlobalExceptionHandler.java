package pl.wrapper.parking.infrastructure.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.wrapper.parking.infrastructure.error.ErrorWrapper;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorWrapper> handleGeneralException(HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "An error has occured";
        ErrorWrapper errorWrapper = new ErrorWrapper(message, status, request.getRequestURI(), status);
        log.error(message);
        return new ResponseEntity<>(errorWrapper, status);
    }

    @ExceptionHandler(InvalidCallException.class)
    public ResponseEntity<ErrorWrapper> handleInvalidCallException(HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Invalid call";
        ErrorWrapper errorWrapper = new ErrorWrapper(message, status, request.getRequestURI(), status);
        log.error(message);
        return new ResponseEntity<>(errorWrapper, status);
    }

    @ExceptionHandler(PwrApiNotRespondingException.class)
    public ResponseEntity<ErrorWrapper> handlePwrApiNotRespondingException(HttpServletRequest request) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        String message = "PWR Api not responding";
        ErrorWrapper errorWrapper = new ErrorWrapper(message, status, request.getRequestURI(), status);
        log.error(message);
        return new ResponseEntity<>(errorWrapper, status);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorWrapper> handleJsonProcessingException(HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Json processing error";
        ErrorWrapper errorWrapper = new ErrorWrapper(message, status, request.getRequestURI(), status);
        log.error(message);
        return new ResponseEntity<>(errorWrapper, status);
    }

    @ExceptionHandler(ClassCastException.class)
    public ResponseEntity<ErrorWrapper> handleClassCastException(HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Class cast exception";
        ErrorWrapper errorWrapper = new ErrorWrapper(message, status, request.getRequestURI(), status);
        log.error(message);
        return new ResponseEntity<>(errorWrapper, status);
    }
}
