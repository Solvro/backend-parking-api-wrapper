package pl.wrapper.parking.infrastructure.inMemory.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;

@Builder
public record ParkingData(
        int parkingId, int totalSpots, Map<DayOfWeek, Map<LocalTime, AvailabilityData>> freeSpotsHistory)
        implements Serializable {}
