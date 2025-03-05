package pl.wrapper.parking.facade.domain.historic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrapper.parking.facade.dto.historicData.HistoricDayData;
import pl.wrapper.parking.facade.dto.historicData.HistoricDayParkingData;
import pl.wrapper.parking.facade.dto.historicData.HistoricPeriodParkingData;
import pl.wrapper.parking.facade.dto.historicData.TimestampEntry;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;

@ExtendWith(MockitoExtension.class)
class ParkingHistoricDataServiceImplTest {

    @Mock
    private PwrApiServerCaller pwrApiServerCaller;

    private final Integer intervalLength = 480;

    @Mock
    private Query atQuery;

    @Mock
    private TypedQuery<HistoricDataEntry> periodQuery;

    @Mock
    private TypedQuery<HistoricDataEntry> fromQuery;

    @Mock
    private EntityManager em;

    @InjectMocks
    @Spy
    private final ParkingHistoricDataServiceImpl parkingHistoricDataService =
            new ParkingHistoricDataServiceImpl(pwrApiServerCaller, intervalLength);

    @Test
    void testGetDataForDay_ValidDate_ReturnsParkingData() {
        LocalDate testDate = LocalDate.of(2023, 10, 1);
        short[][] testData = {
            {10, 20, 30},
            {5, 15, 25}
        };
        List<short[][]> testList = new ArrayList<>();
        testList.add(testData);
        HistoricDayParkingData expectedData = new HistoricDayParkingData(
                (short) 1,
                new HistoricDayData(
                        testDate,
                        List.of(
                                new TimestampEntry("00:00", (short) 5),
                                new TimestampEntry("08:00", (short) 15),
                                new TimestampEntry("16:00", (short) 25))));

        doReturn(atQuery).when(parkingHistoricDataService).createAtQuery(testDate);
        when(atQuery.getResultList()).thenReturn(testList);
        HistoricDayParkingData actualData = parkingHistoricDataService.getDataForDay(testDate, 1);

        assertNotNull(actualData);
        assertEquals(expectedData, actualData);
    }

    @Test
    void testGetDataForDay_EmptyData_ReturnsNull() {
        LocalDate testDate = LocalDate.of(2023, 10, 1);
        doReturn(atQuery).when(parkingHistoricDataService).createAtQuery(testDate);
        when(atQuery.getResultList()).thenReturn(List.of());
        List<HistoricDayParkingData> actualData = parkingHistoricDataService.getDataForDay(testDate);

        assertNull(actualData);
    }

    @Test
    void testGetDataForDay_MultipleParkingLots_ReturnsCorrectData() {
        LocalDate testDate = LocalDate.of(2023, 10, 2);
        short[][] testData = {
            {15, 25, 35},
            {10, 20, 30},
            {5, 10, 15}
        };
        List<short[][]> testList = new ArrayList<>();
        testList.add(testData);
        List<HistoricDayParkingData> expectedData = List.of(
                new HistoricDayParkingData(
                        (short) 0,
                        new HistoricDayData(
                                testDate,
                                List.of(
                                        new TimestampEntry("00:00", (short) 15),
                                        new TimestampEntry("08:00", (short) 25),
                                        new TimestampEntry("16:00", (short) 35)))),
                new HistoricDayParkingData(
                        (short) 1,
                        new HistoricDayData(
                                testDate,
                                List.of(
                                        new TimestampEntry("00:00", (short) 10),
                                        new TimestampEntry("08:00", (short) 20),
                                        new TimestampEntry("16:00", (short) 30)))),
                new HistoricDayParkingData(
                        (short) 2,
                        new HistoricDayData(
                                testDate,
                                List.of(
                                        new TimestampEntry("00:00", (short) 5),
                                        new TimestampEntry("08:00", (short) 10),
                                        new TimestampEntry("16:00", (short) 15)))));

        doReturn(atQuery).when(parkingHistoricDataService).createAtQuery(testDate);
        when(atQuery.getResultList()).thenReturn(testList);
        List<HistoricDayParkingData> actualData = parkingHistoricDataService.getDataForDay(testDate);

        assertNotNull(actualData);
        assertEquals(expectedData.size(), actualData.size());
        assertEquals(expectedData, actualData);
    }

    @Test
    void testGetDataForPeriod_ValidData_ReturnsHistoricPeriodParkingData() {
        LocalDate fromDate = LocalDate.of(2023, 10, 1);
        LocalDate toDate = LocalDate.of(2023, 10, 3);
        int parkingId = 1;

        List<HistoricDataEntry> testData = List.of(
                new HistoricDataEntry(fromDate, new short[][] {
                    {10, 20, 30},
                    {5, 15, 25}
                }),
                new HistoricDataEntry(toDate, new short[][] {
                    {15, 25, 35},
                    {10, 20, 30}
                }));

        doReturn(periodQuery).when(parkingHistoricDataService).createPeriodQuery(fromDate, toDate);
        when(periodQuery.getResultList()).thenReturn(testData);

        HistoricPeriodParkingData actualData = parkingHistoricDataService.getDataForPeriod(fromDate, toDate, parkingId);

        assertNotNull(actualData);
        assertEquals((short) parkingId, actualData.parkingId());
        assertEquals(2, actualData.dataList().size());

        HistoricDayData firstDay = actualData.dataList().getFirst();
        assertEquals(fromDate, firstDay.atDate());
        assertEquals(3, firstDay.data().size());
        assertEquals("00:00", firstDay.data().getFirst().timestamp());
        assertEquals((short) 5, firstDay.data().getFirst().freeSpots());
    }

    @Test
    void testGetDataForPeriod_NoData_ReturnsNull() {
        LocalDate fromDate = LocalDate.of(2023, 10, 1);
        LocalDate toDate = LocalDate.of(2023, 10, 3);
        int parkingId = 1;

        doReturn(periodQuery).when(parkingHistoricDataService).createPeriodQuery(fromDate, toDate);
        when(periodQuery.getResultList()).thenReturn(List.of());

        HistoricPeriodParkingData actualData = parkingHistoricDataService.getDataForPeriod(fromDate, toDate, parkingId);
        assertNull(actualData);
    }

    @Test
    void testGetDataForPeriod_FromDateOnly_ReturnsHistoricData() {
        LocalDate fromDate = LocalDate.of(2023, 10, 1);
        int parkingId = 1;

        List<HistoricDataEntry> testData = List.of(new HistoricDataEntry(fromDate, new short[][] {
            {15, 25, 35},
            {5, 10, 15}
        }));

        doReturn(fromQuery).when(parkingHistoricDataService).createFromQuery(fromDate);
        when(fromQuery.getResultList()).thenReturn(testData);

        HistoricPeriodParkingData actualData = parkingHistoricDataService.getDataForPeriod(fromDate, null, parkingId);

        assertNotNull(actualData);
        assertEquals((short) parkingId, actualData.parkingId());
        assertEquals(1, actualData.dataList().size());
        assertEquals(fromDate, actualData.dataList().getFirst().atDate());
    }

    @Test
    void testCalculateTimeframesCount() throws Exception {
        java.lang.reflect.Method method =
                ParkingHistoricDataServiceImpl.class.getDeclaredMethod("calculateTimeframesCount", int.class);
        method.setAccessible(true);

        int timeframeLength = 60;
        int expectedCount = 24;
        int actualCount = (int) method.invoke(null, timeframeLength);
        assertEquals(expectedCount, actualCount);

        timeframeLength = 30;
        expectedCount = 48;
        actualCount = (int) method.invoke(null, timeframeLength);
        assertEquals(expectedCount, actualCount);

        timeframeLength = 5;
        expectedCount = 288;
        actualCount = (int) method.invoke(null, timeframeLength);
        assertEquals(expectedCount, actualCount);
    }

    @Test
    void testMapTimeToTimeframeIndex() throws Exception {

        java.lang.reflect.Method mapTimeToTimeframeIndexMethod = ParkingHistoricDataServiceImpl.class.getDeclaredMethod(
                "mapTimeToTimeframeIndex", LocalTime.class, int.class);
        mapTimeToTimeframeIndexMethod.setAccessible(true);

        LocalTime time = LocalTime.MIDNIGHT;
        int intervalLength = 60;
        int expectedIndex = 0;
        int actualIndex = (int) mapTimeToTimeframeIndexMethod.invoke(null, time, intervalLength);
        assertEquals(expectedIndex, actualIndex);

        time = LocalTime.of(1, 15);
        expectedIndex = 1;
        actualIndex = (int) mapTimeToTimeframeIndexMethod.invoke(null, time, intervalLength);
        assertEquals(expectedIndex, actualIndex);

        time = LocalTime.of(1, 26);
        intervalLength = 30;
        expectedIndex = 2;
        actualIndex = (int) mapTimeToTimeframeIndexMethod.invoke(null, time, intervalLength);
        assertEquals(expectedIndex, actualIndex);

        time = LocalTime.of(2, 15);
        intervalLength = 15;
        expectedIndex = 9;
        actualIndex = (int) mapTimeToTimeframeIndexMethod.invoke(null, time, intervalLength);
        assertEquals(expectedIndex, actualIndex);

        time = LocalTime.of(9, 0);
        intervalLength = 180;
        expectedIndex = 3;
        actualIndex = (int) mapTimeToTimeframeIndexMethod.invoke(null, time, intervalLength);
        assertEquals(expectedIndex, actualIndex);

        time = LocalTime.of(23, 59);
        intervalLength = 5;
        expectedIndex = 287;
        actualIndex = (int) mapTimeToTimeframeIndexMethod.invoke(null, time, intervalLength);
        assertEquals(expectedIndex, actualIndex);
    }
}
