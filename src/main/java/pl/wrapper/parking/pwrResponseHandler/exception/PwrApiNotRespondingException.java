package pl.wrapper.parking.pwrResponseHandler.exception;

public class PwrApiNotRespondingException extends RuntimeException {
    public PwrApiNotRespondingException(String message) {
        super(message);
    }
}
