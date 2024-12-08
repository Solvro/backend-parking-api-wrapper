package pl.wrapper.parking.pwrResponseHandler.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
        Address address) {

    @JsonIgnore
    public boolean isOpened() {
        LocalTime now = LocalTime.now();
        return openingHours == null || closingHours == null ||
                now.isAfter(openingHours) && now.isBefore(closingHours);
    }
}
