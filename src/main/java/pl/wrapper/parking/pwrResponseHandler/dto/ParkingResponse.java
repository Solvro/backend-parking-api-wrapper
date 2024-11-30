package pl.wrapper.parking.pwrResponseHandler.dto;

import java.time.LocalTime;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Builder
public record ParkingResponse(
        int parkingId,
        int freeSpots,
        int totalSpots,
        String name,
        String symbol,
        @Nullable LocalTime openingHours,
        @Nullable LocalTime closingHours,
        Address address) {}
