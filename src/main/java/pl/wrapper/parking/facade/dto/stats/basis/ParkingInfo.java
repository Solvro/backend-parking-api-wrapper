package pl.wrapper.parking.facade.dto.stats.basis;

import lombok.Builder;

@Builder
public record ParkingInfo(int parkingId, int totalSpots) {}
