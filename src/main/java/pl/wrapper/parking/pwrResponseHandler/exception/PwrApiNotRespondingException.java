package pl.wrapper.parking.pwrResponseHandler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

public class PwrApiNotRespondingException extends RuntimeException {
    public PwrApiNotRespondingException(LocalDateTime calledAt, HttpStatusCode respondedWith) {
        super(calledAt.toString() +  ": Pwr api not responding; It may be error on " + (respondedWith.is4xxClientError() ? "our" : "Pwr server's") + " side.");
    }
}
