package pl.wrapper.parking.facade.dto.historicData;

import io.swagger.v3.oas.annotations.media.Schema;

public record HistoricDayParkingData(
        short parkingId, @Schema(implementation = HistoricDayData.class) HistoricDayData data) {}
