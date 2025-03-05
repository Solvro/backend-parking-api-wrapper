package pl.wrapper.parking.infrastructure.inMemory.dto.parking;

import java.io.Serializable;

public record AvailabilityData(int sampleCount, double averageAvailability) implements Serializable {}
