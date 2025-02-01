package pl.wrapper.parking.facade.dto.main;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NominatimLocation(@JsonProperty("lat") double latitude, @JsonProperty("lon") double longitude) {}
