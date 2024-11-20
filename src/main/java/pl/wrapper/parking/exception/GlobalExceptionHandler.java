package pl.wrapper.parking.exception;

import org.springframework.http.ResponseEntity;

public interface GlobalExceptionHandler {
    ResponseEntity<String> handleGeneralException(Exception e);
    ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e);
    ResponseEntity<String> handleNullPointerException(NullPointerException e);
    ResponseEntity<String> handleIndexOutOfBoundsException(IndexOutOfBoundsException e);
}
