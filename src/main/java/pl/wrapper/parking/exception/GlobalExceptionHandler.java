package pl.wrapper.parking.exception;

import org.springframework.http.ResponseEntity;


public interface GlobalExceptionHandler {
    <T extends Exception> ResponseEntity<String> handleGeneralExceptions(T e);
}
