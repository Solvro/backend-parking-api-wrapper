package pl.wrapper.parking.facade.domain.historic;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrapper.parking.facade.ParkingHistoricDataService;
import pl.wrapper.parking.facade.dto.historicData.HistoricDayData;
import pl.wrapper.parking.facade.dto.historicData.HistoricDayParkingData;
import pl.wrapper.parking.facade.dto.historicData.HistoricPeriodParkingData;
import pl.wrapper.parking.facade.dto.historicData.TimestampEntry;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
class ParkingHistoricDataServiceImpl implements ParkingHistoricDataService {


    @PersistenceContext
    private EntityManager em;

    @Value("${historic.data-update.minutes}")
    private Integer intervalLength;

    private final int intervalCount;

    private final TypedQuery<HistoricDataEntry> periodQuery;

    private final TypedQuery<HistoricDataEntry> fromQuery;

    private final TypedQuery<short[][]> atQuery;

    private final PwrApiServerCaller pwrApiServerCaller;

    private final List<String> formattedStartTimes;

    public ParkingHistoricDataServiceImpl(PwrApiServerCaller pwrApiServerCaller) {
        this.pwrApiServerCaller = pwrApiServerCaller;
        periodQuery = em.createQuery(
                "SELECT data FROM HistoricDataEntry data WHERE data.date >= :from AND data.date <= :to",
                HistoricDataEntry.class);
        fromQuery = em.createQuery(
                "SELECT data FROM HistoricDataEntry data WHERE data.date >= :from",
                HistoricDataEntry.class);
        atQuery = em.createQuery(
                "SELECT data.parkingInfo FROM HistoricDataEntry data WHERE data.date = :at",
                short[][].class);
        intervalCount = calculateTimeframesCount(intervalLength);
        this.formattedStartTimes = getFormattedStartTimes(intervalLength, intervalCount);
    }

    @Override
    public List<HistoricDayParkingData> getDataForDay(LocalDate forDate) {
        List<short[][]> fetchedData = atQuery.setParameter("at", forDate).getResultList();
        if (fetchedData.isEmpty()) return null;
        return parseTableForDay(fetchedData.getFirst(), forDate);
    }

    @Override
    public HistoricDayParkingData getDataForDay(LocalDate forDate, int parkingId) {
        List<short[][]> fetchedData = atQuery.setParameter("at", forDate).getResultList();
        if (fetchedData.isEmpty()) return null;
        return parseTableForDay(parkingId, fetchedData.getFirst(), forDate);
    }

    @Override
    public HistoricPeriodParkingData getDataForPeriod(LocalDate fromDate, LocalDate toDate, int parkingId) {
        List<HistoricDataEntry> fetchedData;
        if (toDate == null) fetchedData = fromQuery.setParameter("from", fromDate).getResultList();
        else fetchedData = periodQuery.setParameter("from", fromDate).setParameter("to", toDate).getResultList();
        if (fetchedData.isEmpty()) return null;
        return parseTableForPeriod(fetchedData, parkingId);
    }

    @Override
    public List<HistoricPeriodParkingData> getDataForPeriod(LocalDate fromDate, LocalDate toDate) {
        List<HistoricDataEntry> fetchedData;
        if (toDate == null) fetchedData = fromQuery.setParameter("from", fromDate).getResultList();
        else fetchedData = periodQuery.setParameter("from", fromDate).setParameter("to", toDate).getResultList();
        if (fetchedData.isEmpty()) return null;
        return parseTableForPeriod(fetchedData);
    }

    private HistoricDayParkingData parseTableForDay(int parkingId, short[][] dataTable, LocalDate forDate) {
        if (parkingId >= dataTable.length) return null;
        return new HistoricDayParkingData(
                (short) parkingId,
                new HistoricDayData(forDate,
                        getTimestampedList(dataTable, parkingId))
        );
    }

    private List<HistoricDayParkingData> parseTableForDay(short[][] dataTable, LocalDate forDate) {
        List<HistoricDayParkingData> resultList = new ArrayList<>(dataTable.length + 1);
        for (int i = 0; i < dataTable.length; i++)
            resultList.add(
                    new HistoricDayParkingData((short) i,
                            new HistoricDayData(forDate,
                                    getTimestampedList(dataTable, i)))
            );
        return resultList;
    }

    private List<TimestampEntry> getTimestampedList(short[][] dataTable, int parkingId) {
        int bound = dataTable[parkingId].length;
        List<TimestampEntry> entryList = new ArrayList<>( bound+ 1);
        for (int j = 0; j < bound; j++)
            entryList.add(new TimestampEntry(formattedStartTimes.get(j), dataTable[parkingId][j]));
        return entryList;
    }

    private HistoricPeriodParkingData parseTableForPeriod(List<HistoricDataEntry> dataEntries, int parkingId) {
        return new HistoricPeriodParkingData(
                (short) parkingId,
                dataEntries.stream()
                        .map(data -> new HistoricDayData(data.getDate(), getTimestampedList(data.getParkingInfo(), parkingId)))
                        .toList());
    }

    private List<HistoricPeriodParkingData> parseTableForPeriod(List<HistoricDataEntry> dataEntries) {
        int parkingCount = dataEntries.getFirst().getParkingInfo().length;
        List<List<HistoricDayData>> dataLists = new ArrayList<>(parkingCount + 1);
        List<HistoricPeriodParkingData> resultList = new ArrayList<>(parkingCount + 1);
        for (int i = 0; i < parkingCount; i++) dataLists.add(new ArrayList<>());

        dataEntries.forEach(data -> {
            short[][] dataTable = data.getParkingInfo();
            LocalDate currentDate = data.getDate();
            for (int i = 0; i < dataTable.length; i++)
                dataLists.get(i).add(new HistoricDayData(currentDate, getTimestampedList(dataTable, i)));
        });
        for (int i = 0; i < parkingCount; i++)
            resultList.add(new HistoricPeriodParkingData((short) i, dataLists.get(i)));
        return resultList;
    }

    @Scheduled(cron = "30 */${historic.data-update.minutes} * * * *")
    @Transactional
    void storeNewData() {
        List<ParkingResponse> fetchedData = pwrApiServerCaller.fetchParkingData();
        LocalDate today = LocalDate.now();
        HistoricDataEntry entryForToday = em.find(HistoricDataEntry.class, today);
        if (entryForToday == null)
            entryForToday = new HistoricDataEntry(fetchedData.size(), intervalCount, today);
        int currentIntervalIndex = mapTimeToTimeframeIndex(LocalTime.now(), intervalLength);
        for (ParkingResponse parkingData : fetchedData) {
            entryForToday.addValue(parkingData.parkingId(), currentIntervalIndex, parkingData.freeSpots());
        }
    }

    private static int calculateTimeframesCount(int timeframeLengthInMinutes) {
        return (int) Math.ceil((double) 24 * 60 / timeframeLengthInMinutes);
    }

    private static int mapTimeToTimeframeIndex(LocalTime time, int intervalLength) {
        return (int) ChronoUnit.MINUTES.between(LocalTime.MIDNIGHT, time) / intervalLength;
    }

    private static List<String> getFormattedStartTimes(int intervalLength, int intervalCount) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime currentTime = LocalTime.MIDNIGHT;
        List<String> formattedStartTimes = new ArrayList<>();
        for (int i = 0; i < intervalCount; i++) {
            formattedStartTimes.add(currentTime.format(formatter));
            currentTime = currentTime.plusMinutes(intervalLength);
        }
        return formattedStartTimes;
    }
}
