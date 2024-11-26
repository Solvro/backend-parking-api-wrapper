package pl.wrapper.parking.infrastructure.error;

public sealed interface ParkingError extends Error {
    record ParkingNotFoundBySymbol(String symbol) implements ParkingError {}
}
