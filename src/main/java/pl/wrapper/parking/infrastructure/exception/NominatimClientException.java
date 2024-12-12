package pl.wrapper.parking.infrastructure.exception;

public class NominatimClientException extends RuntimeException {
    public NominatimClientException(String message) {
        super(message);
    }
}
