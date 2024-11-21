package pl.wrapper.parking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static pl.wrapper.parking.exception.ExceptionStatus.getStatusForException;


@ControllerAdvice
public class GlobalExceptionHandlerImpl implements GlobalExceptionHandler{

    @ExceptionHandler()
    @Override
    public <T extends Exception> ResponseEntity<String> handleGeneralExceptions(T e) {
        String message = e.getClass().getSimpleName() + ": " + e.getMessage();
        HttpStatus status = getStatusForException(e.getClass());
        return new ResponseEntity<>(message, status);
    }
}