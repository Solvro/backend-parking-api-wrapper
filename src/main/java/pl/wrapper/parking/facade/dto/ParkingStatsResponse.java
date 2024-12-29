package pl.wrapper.parking.facade.dto;

import java.time.LocalDateTime;

public record ParkingStatsResponse(long totalUsage, double averageAvailability, LocalDateTime peakOccupancyAt) {
}
