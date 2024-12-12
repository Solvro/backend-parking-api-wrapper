package pl.wrapper.parking.infrastructure.error;

public sealed interface ParkingError extends Error {
    // remember to add newly error to connected to its controller
    record ParkingNotFoundBySymbol(String symbol) implements ParkingError {}

    record ParkingNotFoundById(Integer id) implements ParkingError {}

    record ParkingNotFoundByName(String name) implements ParkingError {}

    record ParkingNotFoundByAddress(String address) implements ParkingError {}
}
