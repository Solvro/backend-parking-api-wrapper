package pl.wrapper.parking.facade;

import org.springframework.lang.Nullable;
import pl.wrapper.parking.facade.dto.historicData.HistoricDayParkingData;
import pl.wrapper.parking.facade.dto.historicData.HistoricPeriodParkingData;

import java.time.LocalDate;
import java.util.List;

public interface ParkingHistoricDataService {
    List<HistoricDayParkingData> getDataForDay(LocalDate forDate);
    HistoricDayParkingData getDataForDay(LocalDate forDate, int parkingId);
    HistoricPeriodParkingData getDataForPeriod(LocalDate fromDate, @Nullable LocalDate toDate, int parkingId);
    List<HistoricPeriodParkingData> getDataForPeriod(LocalDate fromDate, @Nullable LocalDate toDate);
}
