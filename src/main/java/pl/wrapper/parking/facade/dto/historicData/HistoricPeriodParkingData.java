package pl.wrapper.parking.facade.dto.historicData;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record HistoricPeriodParkingData(short parkingId, @ArraySchema(schema = @Schema(implementation = HistoricDayData.class)) List<HistoricDayData> dataList) {
}
