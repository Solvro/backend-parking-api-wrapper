package pl.wrapper.parking.facade.dto.stats.basis;

import lombok.Builder;

@Builder
public record ParkingStats(double averageAvailability, int averageFreeSpots) {}
