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
class GlobalExceptionHandler{

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorWrapper> handleGeneralException(Exception e, HttpServletRequest request) {
        ErrorWrapper errorWrapper = new ErrorWrapper(e.getMessage(), HttpStatus.I_AM_A_TEAPOT, request.getRequestURI());
        return new ResponseEntity<>(errorWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidCallException.class)
    public ResponseEntity<ErrorWrapper> handleInvalidCallException(InvalidCallException e,
                                                                   HttpServletRequest request) {
        ErrorWrapper errorWrapper = new ErrorWrapper(e.getMessage(), HttpStatus.I_AM_A_TEAPOT, request.getRequestURI());
        return new ResponseEntity<>(errorWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(PwrApiNotRespondingException.class)
    public ResponseEntity<ErrorWrapper> handlePwrApiNotRespondingException(PwrApiNotRespondingException e,
                                                                           HttpServletRequest request) {
        ErrorWrapper errorWrapper = new ErrorWrapper(e.getMessage(),HttpStatus.I_AM_A_TEAPOT, request.getRequestURI());
        return new ResponseEntity<>(errorWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorWrapper> handleJsonProcessingException(JsonProcessingException e,
                                                                      HttpServletRequest request) {
        ErrorWrapper errorWrapper = new ErrorWrapper(e.getMessage(),HttpStatus.I_AM_A_TEAPOT, request.getRequestURI());
        return new ResponseEntity<>(errorWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorWrapper> handleIllegalArgumentException(IllegalArgumentException e,
                                                                       HttpServletRequest request) {
        ErrorWrapper errorWrapper = new ErrorWrapper(e.getMessage(),HttpStatus.I_AM_A_TEAPOT, request.getRequestURI());
        return new ResponseEntity<>(errorWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorWrapper> handleNullPointerException(NullPointerException e,
                                                                   HttpServletRequest request) {
        ErrorWrapper errorWrapper = new ErrorWrapper(e.getMessage(),HttpStatus.I_AM_A_TEAPOT, request.getRequestURI());
        return new ResponseEntity<>(errorWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ClassCastException.class)
    public ResponseEntity<ErrorWrapper> handleNullPointerException(ClassCastException e,
                                                                   HttpServletRequest request) {
        ErrorWrapper errorWrapper = new ErrorWrapper(e.getMessage(),HttpStatus.I_AM_A_TEAPOT, request.getRequestURI());
        return new ResponseEntity<>(errorWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}