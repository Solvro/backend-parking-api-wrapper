package pl.wrapper.parking.infrastructure.inMemory.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ParkingData(int parkingId, int freeSpots, int totalSpots, LocalDateTime timestamp) implements Serializable {
    public record CompositeKey(int parkingId, LocalDateTime timestamp) implements Serializable {}
}
