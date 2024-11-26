package pl.wrapper.parking.infrastructure.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
class GlobalExceptionHandler{

    //ErrorWrapper
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e, HttpServletRequest request) {
        return new ResponseEntity<>("An error has occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        //body.uri -> req.getUri()
    }

    @ExceptionHandler(InvalidCallException.class)
    public ResponseEntity<String> handleInvalidCallException(InvalidCallException e) {
        return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //Add handling for JsonProcessingException, PwrApi... and other that may be needed
}