package pl.wrapper.parking.facade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NominatimLocation {

    @JsonProperty("lat")
    private double latitude;

    @JsonProperty("lon")
    private double longitude;
}
