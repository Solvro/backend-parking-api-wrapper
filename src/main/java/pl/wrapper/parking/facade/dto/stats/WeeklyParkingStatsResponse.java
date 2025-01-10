package pl.wrapper.parking.facade.dto.stats;

public record WeeklyParkingStatsResponse(
        ParkingStats stats, OccupancyInfo maxOccupancyInfo, OccupancyInfo minOccupancyInfo) {}
