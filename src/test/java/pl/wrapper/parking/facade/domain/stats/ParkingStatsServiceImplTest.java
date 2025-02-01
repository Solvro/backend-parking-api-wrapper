package pl.wrapper.parking.facade.domain.stats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrapper.parking.facade.dto.stats.ParkingStatsResponse;
import pl.wrapper.parking.facade.dto.stats.basis.OccupancyInfo;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingInfo;
import pl.wrapper.parking.facade.dto.stats.basis.ParkingStats;
import pl.wrapper.parking.facade.dto.stats.daily.CollectiveDailyParkingStats;
import pl.wrapper.parking.facade.dto.stats.daily.DailyParkingStatsResponse;
import pl.wrapper.parking.facade.dto.stats.weekly.CollectiveWeeklyParkingStats;
import pl.wrapper.parking.facade.dto.stats.weekly.WeeklyParkingStatsResponse;
import pl.wrapper.parking.infrastructure.inMemory.ParkingDataRepository;
import pl.wrapper.parking.infrastructure.inMemory.dto.AvailabilityData;
import pl.wrapper.parking.infrastructure.inMemory.dto.ParkingData;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.DayOfWeek.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingStatsServiceImplTest {

    @Mock
    private ParkingDataRepository dataRepository;

    private ParkingStatsServiceImpl parkingStatsService;

    private List<ParkingData> parkingData;

    @BeforeEach
    void setUp() {
        parkingStatsService = new ParkingStatsServiceImpl(dataRepository, 10);
        parkingData = List.of(
                ParkingData.builder()
                        .parkingId(1)
                        .totalSpots(100)
                        .freeSpotsHistory(Map.of(
                                MONDAY,
                                Map.of(
                                        LocalTime.of(10, 0), new AvailabilityData(1, 0.8),
                                        LocalTime.of(12, 0), new AvailabilityData(1, 0.5)),
                                TUESDAY, Map.of(LocalTime.of(10, 0), new AvailabilityData(1, 0.7))))
                        .build(),
                ParkingData.builder()
                        .parkingId(2)
                        .totalSpots(200)
                        .freeSpotsHistory(Map.of(
                                MONDAY, Map.of(LocalTime.of(10, 0), new AvailabilityData(1, 0.6)),
                                WEDNESDAY, Map.of(LocalTime.of(14, 0), new AvailabilityData(1, 0.9))))
                        .build());
    }

    @Test
    void getParkingStats_withDayOfWeekAndTime_returnCorrectStats() {
        when(dataRepository.values()).thenReturn(parkingData);

        List<ParkingStatsResponse> result = parkingStatsService.getParkingStats(null, MONDAY, LocalTime.of(10, 7));

        assertThat(result).hasSize(2);
        ParkingStatsResponse stats1 = result.get(0);
        ParkingStatsResponse stats2 = result.get(1);
        assertThat(stats1.parkingInfo())
                .extracting(ParkingInfo::parkingId, ParkingInfo::totalSpots)
                .containsExactly(1, 100);
        assertThat(stats1.stats())
                .extracting(ParkingStats::averageAvailability, ParkingStats::averageFreeSpots)
                .containsExactly(0.8, 80);

        assertThat(stats2.parkingInfo())
                .extracting(ParkingInfo::parkingId, ParkingInfo::totalSpots)
                .containsExactly(2, 200);
        assertThat(stats2.stats())
                .extracting(ParkingStats::averageAvailability, ParkingStats::averageFreeSpots)
                .containsExactly(0.6, 120);
    }

    @Test
    void getParkingStats_withWeirdIdListAndTime_returnCorrectStats() {
        when(dataRepository.fetchAllKeys()).thenReturn(Set.of(1, 2));
        when(dataRepository.get(anyInt())).thenReturn(parkingData.get(0), parkingData.get(1));

        List<ParkingStatsResponse> result = parkingStatsService.getParkingStats(List.of(1, 2, 3), null, LocalTime.of(10, 7));

        assertThat(result).hasSize(2);
        ParkingStatsResponse stats1 = result.get(0);
        ParkingStatsResponse stats2 = result.get(1);
        assertThat(stats1.parkingInfo())
                .extracting(ParkingInfo::parkingId, ParkingInfo::totalSpots)
                .containsExactly(1, 100);
        assertThat(stats1.stats())
                .extracting(ParkingStats::averageAvailability, ParkingStats::averageFreeSpots)
                .containsExactly(0.75, 75);

        assertThat(stats2.parkingInfo())
                .extracting(ParkingInfo::parkingId, ParkingInfo::totalSpots)
                .containsExactly(2, 200);
        assertThat(stats2.stats())
                .extracting(ParkingStats::averageAvailability, ParkingStats::averageFreeSpots)
                .containsExactly(0.6, 120);

        verify(dataRepository, never()).values();
    }

    @Test
    void getParkingStats_withEmptyDataRepository_returnEmptyList() {
        when(dataRepository.values()).thenReturn(List.of());

        List<ParkingStatsResponse> result = parkingStatsService.getParkingStats(List.of(1, 2), MONDAY, LocalTime.of(10, 7));

        assertThat(result).isEmpty();
    }

    @Test
    void getDailyParkingStats_withIdList_returnCorrectDailyStats() {
        when(dataRepository.fetchAllKeys()).thenReturn(Set.of(1, 2));
        when(dataRepository.get(1)).thenReturn(parkingData.getFirst());

        List<DailyParkingStatsResponse> result = parkingStatsService.getDailyParkingStats(List.of(1), MONDAY);

        assertThat(result).hasSize(1);
        DailyParkingStatsResponse stats = result.getFirst();
        assertThat(stats.parkingInfo())
                .extracting(ParkingInfo::parkingId, ParkingInfo::totalSpots)
                .containsExactly(1, 100);
        assertThat(stats.stats())
                .extracting(ParkingStats::averageAvailability, ParkingStats::averageFreeSpots)
                .containsExactly(0.65, 65);
        assertThat(stats)
                .extracting(DailyParkingStatsResponse::maxOccupancyAt, DailyParkingStatsResponse::minOccupancyAt)
                .containsExactly(LocalTime.of(12, 0), LocalTime.of(10, 0));

        verify(dataRepository, never()).values();
    }

    @Test
    void getWeeklyParkingStats_withEmptyIdList_returnCorrectWeeklyStats() {
        when(dataRepository.values()).thenReturn(parkingData);

        List<WeeklyParkingStatsResponse> result = parkingStatsService.getWeeklyParkingStats(List.of());

        assertThat(result).hasSize(2);
        WeeklyParkingStatsResponse stats1 = result.get(0);
        WeeklyParkingStatsResponse stats2 = result.get(1);
        assertThat(stats1.parkingInfo())
                .extracting(ParkingInfo::parkingId, ParkingInfo::totalSpots)
                .containsExactly(1, 100);
        assertThat(stats1.stats().averageAvailability()).isCloseTo(0.666, within(0.001));
        assertThat(stats1.stats().averageFreeSpots()).isCloseTo(66, within(1));
        assertThat(stats1.maxOccupancyInfo())
                .extracting(OccupancyInfo::dayOfWeek, OccupancyInfo::time)
                .containsExactly(MONDAY, LocalTime.of(12, 0));
        assertThat(stats1.minOccupancyInfo())
                .extracting(OccupancyInfo::dayOfWeek, OccupancyInfo::time)
                .containsExactly(MONDAY, LocalTime.of(10, 0));

        assertThat(stats2.parkingInfo())
                .extracting(ParkingInfo::parkingId, ParkingInfo::totalSpots)
                .containsExactly(2, 200);
        assertThat(stats2.stats())
                .extracting(ParkingStats::averageAvailability, ParkingStats::averageFreeSpots)
                .containsExactly(0.75, 150);
        assertThat(stats2.maxOccupancyInfo())
                .extracting(OccupancyInfo::dayOfWeek, OccupancyInfo::time)
                .containsExactly(MONDAY, LocalTime.of(10, 0));
        assertThat(stats2.minOccupancyInfo())
                .extracting(OccupancyInfo::dayOfWeek, OccupancyInfo::time)
                .containsExactly(WEDNESDAY, LocalTime.of(14, 0));
    }

    @Test
    void getCollectiveDailyParkingStats_withWeirdIdList_returnCorrectCollectiveDailyStats() {
        when(dataRepository.fetchAllKeys()).thenReturn(Set.of(1, 2));
        when(dataRepository.get(1)).thenReturn(parkingData.getFirst());

        List<CollectiveDailyParkingStats> result =
                parkingStatsService.getCollectiveDailyParkingStats(List.of(-7, 1, 100), MONDAY);

        assertThat(result).hasSize(1);
        CollectiveDailyParkingStats stats = result.getFirst();
        assertThat(stats.parkingInfo())
                .extracting(ParkingInfo::parkingId, ParkingInfo::totalSpots)
                .containsExactly(1, 100);
        assertThat(stats.statsMap())
                .containsOnly(
                        entry(LocalTime.of(10, 0), new ParkingStats(0.8, 80)),
                        entry(LocalTime.of(12, 0), new ParkingStats(0.5, 50)));

        verify(dataRepository, never()).values();
    }

    @Test
    void getCollectiveWeeklyParkingStats_withoutIdList_returnCorrectCollectiveWeeklyStats() {
        when(dataRepository.values()).thenReturn(parkingData);

        List<CollectiveWeeklyParkingStats> result = parkingStatsService.getCollectiveWeeklyParkingStats(null);

        assertThat(result).hasSize(2);
        CollectiveWeeklyParkingStats stats1 = result.get(0);
        CollectiveWeeklyParkingStats stats2 = result.get(1);
        assertThat(stats1.parkingInfo())
                .extracting(ParkingInfo::parkingId, ParkingInfo::totalSpots)
                .containsExactly(1, 100);
        assertThat(stats1.statsMap())
                .containsOnly(
                        entry(
                                MONDAY,
                                Map.of(
                                        LocalTime.of(10, 0), new ParkingStats(0.8, 80),
                                        LocalTime.of(12, 0), new ParkingStats(0.5, 50))),
                        entry(TUESDAY, Map.of(LocalTime.of(10, 0), new ParkingStats(0.7, 70))));

        assertThat(stats2.parkingInfo())
                .extracting(ParkingInfo::parkingId, ParkingInfo::totalSpots)
                .containsExactly(2, 200);
        assertThat(stats2.statsMap())
                .containsOnly(
                        entry(MONDAY, Map.of(LocalTime.of(10, 0), new ParkingStats(0.6, 120))),
                        entry(WEDNESDAY, Map.of(LocalTime.of(14, 0), new ParkingStats(0.9, 180))));
    }
}
