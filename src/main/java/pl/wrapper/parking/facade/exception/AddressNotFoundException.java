package pl.wrapper.parking.facade.exception;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(String address) {
        super("Geocoding failed: No results for the provided address: " + address);
    }
}
