package pl.wrapper.parking.facade.exception;

public class NominatimClientException extends RuntimeException {
    public NominatimClientException(String message) {
        super(message);
    }
}
