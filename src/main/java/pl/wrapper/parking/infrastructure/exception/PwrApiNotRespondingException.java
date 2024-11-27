package pl.wrapper.parking.infrastructure.exception;

public class PwrApiNotRespondingException extends RuntimeException {
    public PwrApiNotRespondingException(String message) {
        super(message);
    }
}
