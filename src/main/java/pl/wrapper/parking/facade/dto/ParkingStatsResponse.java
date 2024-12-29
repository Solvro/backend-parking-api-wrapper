package pl.wrapper.parking.facade.dto;

import java.time.LocalDateTime;

public record ParkingStatsResponse(int totalUsage, double averageAvailability, LocalDateTime peakOccupancyAt) {
}
