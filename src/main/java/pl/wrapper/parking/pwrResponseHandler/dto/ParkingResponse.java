package pl.wrapper.parking.pwrResponseHandler.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Builder
public record ParkingResponse(
        int parkingId,
        int freeSpots,
        int totalSpots,
        @Schema(example = "best parking")
        String name,
        @Schema(example = "WRO")
        String symbol,
        @Schema(type = "string", format = "time", example = "08:00:00")
        @Nullable LocalTime openingHours,
        @Schema(type = "string", format = "time", example = "22:00:00")
        @Nullable LocalTime closingHours,
        Address address) {

    @JsonIgnore
    public boolean isOpened() {
        LocalTime now = LocalTime.now();
        return openingHours == null || closingHours == null ||
                now.isAfter(openingHours) && now.isBefore(closingHours);
    }
}
