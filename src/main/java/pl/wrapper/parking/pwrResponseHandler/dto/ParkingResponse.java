package pl.wrapper.parking.pwrResponseHandler.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Builder
public record ParkingResponse(
        @Schema(example = "4") int parkingId,
        @Schema(example = "33") int freeSpots,
        @Schema(example = "97") int totalSpots,
        @Schema(example = "best parking") String name,
        @Schema(example = "WRO") String symbol,
        @Schema(type = "string", format = "time", example = "08:00:00") @Nullable LocalTime openingHours,
        @Schema(type = "string", format = "time", example = "22:00:00") @Nullable LocalTime closingHours,
        @Schema(implementation = Address.class) Address address) {

    @JsonIgnore
    public boolean isOpened() {
        LocalTime now = LocalTime.now();
        return openingHours == null || closingHours == null || now.isAfter(openingHours) && now.isBefore(closingHours);
    }
}
