package pl.wrapper.parking.facade.domain.stats.request;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrapper.parking.facade.dto.stats.request.EndpointStats;
import pl.wrapper.parking.infrastructure.inMemory.ParkingRequestRepository;
import pl.wrapper.parking.infrastructure.inMemory.dto.request.EndpointData;
import pl.wrapper.parking.infrastructure.inMemory.dto.request.TimeframeStatistic;

@ExtendWith(MockitoExtension.class)
public class ParkingRequestStatsServiceImplTest {

    @Mock
    private ParkingRequestRepository requestRepository;

    private ParkingRequestStatsServiceImpl requestStatsService;

    private Set<Map.Entry<String, EndpointData>> dataEntries;

    @BeforeEach
    void setUp() {
        EndpointData totalEndpoint = EndpointData.builder()
                .timeframeStatistics(new TimeframeStatistic[] {
                    new TimeframeStatistic(10.0, 9, 4),
                    new TimeframeStatistic(15.0, 15, 4),
                    new TimeframeStatistic(23.0, 21, 3),
                    new TimeframeStatistic(18.4, 17, 4),
                    new TimeframeStatistic(21.2, 19, 5)
                })
                .successCount(46)
                .requestCount(50)
                .timeframeLength(30)
                .build();

        when(requestRepository.getTotalEndpoint()).thenReturn(totalEndpoint);

        requestStatsService = new ParkingRequestStatsServiceImpl(requestRepository);

        dataEntries = Set.of(
                Map.entry(
                        "parkings/free",
                        EndpointData.builder()
                                .successCount(10)
                                .requestCount(12)
                                .timeframeLength(30)
                                .timeframeStatistics(new TimeframeStatistic[] {
                                    new TimeframeStatistic(2.0, 5, 2),
                                    new TimeframeStatistic(3.0, 7, 3),
                                    new TimeframeStatistic(4.0, 8, 3)
                                })
                                .build()),
                Map.entry(
                        "parkings",
                        EndpointData.builder()
                                .successCount(26)
                                .requestCount(29)
                                .timeframeLength(30)
                                .timeframeStatistics(new TimeframeStatistic[] {
                                    new TimeframeStatistic(9.1, 12, 7),
                                    new TimeframeStatistic(13.2, 14, 8),
                                    new TimeframeStatistic(18.7, 20, 7),
                                    new TimeframeStatistic(14.4, 19, 9)
                                })
                                .build()),
                Map.entry(
                        "parkings/address",
                        EndpointData.builder()
                                .successCount(6)
                                .requestCount(13)
                                .timeframeLength(30)
                                .timeframeStatistics(new TimeframeStatistic[] {
                                    new TimeframeStatistic(2.5, 5, 4), new TimeframeStatistic(3.0, 6, 4)
                                })
                                .build()));
    }

    @Test
    void getBasicRequestStats_shouldReturnData() {
        when(requestRepository.fetchAllEntries()).thenReturn(dataEntries);

        Map<String, EndpointStats> result = requestStatsService.getBasicRequestStats();

        assertThat(result).hasSize(3);
        assertThat(result.get("parkings/free"))
                .extracting(EndpointStats::successfulRequests, EndpointStats::totalRequests, EndpointStats::successRate)
                .containsExactly(10L, 12L, 83.33);
        assertThat(result.get("parkings"))
                .extracting(EndpointStats::successfulRequests, EndpointStats::totalRequests, EndpointStats::successRate)
                .containsExactly(26L, 29L, 89.66);
        assertThat(result.get("parkings/address"))
                .extracting(EndpointStats::successfulRequests, EndpointStats::totalRequests, EndpointStats::successRate)
                .containsExactly(6L, 13L, 46.15);
    }

    @Test
    void getBasicRequestStats_shouldReturnEmptyData() {
        when(requestRepository.fetchAllEntries()).thenReturn(Collections.emptySet());

        Map<String, EndpointStats> result = requestStatsService.getBasicRequestStats();

        assertThat(result).isEmpty();
    }

    @Test
    void getRequestStatsForTimes_shouldReturnData() {
        when(requestRepository.fetchAllEntries()).thenReturn(dataEntries);

        Map<String, List<Map.Entry<String, Double>>> result = requestStatsService.getRequestStatsForTimes();

        assertThat(result).hasSize(3);
        assertThat(result.get("parkings/free"))
                .extracting(Map.Entry::getKey, Map.Entry::getValue)
                .containsExactly(tuple("00:00 - 00:30", 2.0), tuple("00:30 - 01:00", 3.0), tuple("01:00 - 01:30", 4.0));
        assertThat(result.get("parkings"))
                .extracting(Map.Entry::getKey, Map.Entry::getValue)
                .containsExactly(
                        tuple("00:00 - 00:30", 9.1),
                        tuple("00:30 - 01:00", 13.2),
                        tuple("01:00 - 01:30", 18.7),
                        tuple("01:30 - 02:00", 14.4));
        assertThat(result.get("parkings/address"))
                .extracting(Map.Entry::getKey, Map.Entry::getValue)
                .containsExactly(tuple("00:00 - 00:30", 2.5), tuple("00:30 - 01:00", 3.0));
    }

    @Test
    void getRequestStatsForTimes_shouldReturnEmptyData() {
        when(requestRepository.fetchAllEntries()).thenReturn(Collections.emptySet());

        Map<String, List<Map.Entry<String, Double>>> result = requestStatsService.getRequestStatsForTimes();

        assertThat(result).isEmpty();
    }

    @Test
    void getRequestPeakTimes_shouldReturnData() {
        List<Map.Entry<String, Double>> result = requestStatsService.getRequestPeakTimes();

        assertThat(result)
                .hasSize(3)
                .extracting(Map.Entry::getKey, Map.Entry::getValue)
                .containsExactly(
                        tuple("01:00 - 01:30", 23.0), tuple("02:00 - 02:30", 21.2), tuple("01:30 - 02:00", 18.4));
    }

    @Test
    void getRequestPeakTimes_shouldReturnEmptyData() {
        when(requestRepository.getTotalEndpoint())
                .thenReturn(EndpointData.builder()
                        .timeframeStatistics(new TimeframeStatistic[] {})
                        .build());

        List<Map.Entry<String, Double>> result = requestStatsService.getRequestPeakTimes();

        assertThat(result).isEmpty();
    }

    @Test
    void getRequestPeakTimes_missingTimeframe_shouldReturnData() {
        when(requestRepository.getTotalEndpoint())
                .thenReturn(EndpointData.builder()
                        .timeframeStatistics(new TimeframeStatistic[] {
                            new TimeframeStatistic(26.5, 18, 5), new TimeframeStatistic(31.7, 21, 6),
                        })
                        .build());

        List<Map.Entry<String, Double>> result = requestStatsService.getRequestPeakTimes();

        assertThat(result)
                .hasSize(2)
                .extracting(Map.Entry::getKey, Map.Entry::getValue)
                .containsExactly(tuple("00:30 - 01:00", 31.7), tuple("00:00 - 00:30", 26.5));
    }

    @Test
    void getDailyRequestStats_shouldReturnData() {
        when(requestRepository.fetchAllEntries()).thenReturn(dataEntries);

        Map<String, Double> result = requestStatsService.getDailyRequestStats();

        assertThat(result).hasSize(3);
        assertThat(result.get("parkings/free")).isEqualTo(9.0);
        assertThat(result.get("parkings")).isEqualTo(55.4);
        assertThat(result.get("parkings/address")).isEqualTo(5.5);
    }

    @Test
    void getDailyRequestStats_shouldReturnEmptyData() {
        when(requestRepository.fetchAllEntries()).thenReturn(Collections.emptySet());

        Map<String, Double> result = requestStatsService.getDailyRequestStats();

        assertThat(result).isEmpty();
    }
}
