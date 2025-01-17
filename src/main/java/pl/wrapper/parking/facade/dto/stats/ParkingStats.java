package pl.wrapper.parking.facade.dto.stats;

import lombok.Builder;

@Builder
public record ParkingStats(int parkingId, double averageAvailability, int averageFreeSpots) {}
