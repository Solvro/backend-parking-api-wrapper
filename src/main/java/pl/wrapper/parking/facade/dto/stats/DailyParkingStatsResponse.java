package pl.wrapper.parking.facade.dto.stats;

import java.time.LocalTime;

public record DailyParkingStatsResponse(ParkingStats stats, LocalTime maxOccupancyAt, LocalTime minOccupancyAt) {}
