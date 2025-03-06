package pl.wrapper.parking.facade.domain.stats.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrapper.parking.facade.ParkingRequestStatsService;
import pl.wrapper.parking.facade.dto.stats.request.EndpointStats;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/stats/requests")
class ParkingRequestStatsController {
    private final ParkingRequestStatsService parkingRequestStatsService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, EndpointStats>> getBasicRequestStats() {
        log.info("Fetching parking requests stats");
        Map<String, EndpointStats> result = parkingRequestStatsService.getBasicRequestStats();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @GetMapping(path = "/times", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<Map.Entry<String, Double>>>> getRequestStatsForTimes() {
        log.info("Fetching parking requests stats for times");
        Map<String, List<Map.Entry<String, Double>>> result = parkingRequestStatsService.getRequestStatsForTimes();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @GetMapping(path = "/peak-times", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map.Entry<String, Double>>> getRequestPeakTimes() {
        log.info("Fetching peak request times");
        List<Map.Entry<String, Double>> result = parkingRequestStatsService.getRequestPeakTimes();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @GetMapping(path = "/day", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Double>> getDailyRequestStats() {
        log.info("Fetching parking requests stats daily");
        Map<String, Double> result = parkingRequestStatsService.getDailyRequestStats();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
