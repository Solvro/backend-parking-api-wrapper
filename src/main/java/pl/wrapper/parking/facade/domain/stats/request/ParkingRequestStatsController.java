package pl.wrapper.parking.facade.domain.stats.request;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
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

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Parking API Request Stats", description = "Endpoints for request statistics")
@RequestMapping("/stats/requests")
class ParkingRequestStatsController {
    private final ParkingRequestStatsService parkingRequestStatsService;

    @Operation(
            summary = "get basic request statistics",
            description = "Returns stats as request count, successful request count, success rate for each endpoint")
    @ApiResponse(
            responseCode = "200",
            description = "map of basic statistics, key - endpoint name, value - endpoint stats",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples =
                                    @ExampleObject(
                                            name = "example",
                                            description = "key: String -> value: EndpointStats",
                                            value =
                                                    "{ parkings/free: { totalRequests: 3, successfulRequests: 2, successRate: 0.67 }, parkings: { totalRequests: 5, successfulRequests: 3, successRate: 0.6 } }")))
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, EndpointStats>> getBasicRequestStats() {
        log.info("Fetching parking requests stats");
        Map<String, EndpointStats> result = parkingRequestStatsService.getBasicRequestStats();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(
            summary = "get request statistics in timeframes",
            description = "Returns the list of pairs(timeframe, average number of requests) for each endpoint")
    @ApiResponse(
            responseCode = "200",
            description =
                    "map of statistics in timeframes, key - endpoint name, value - list of timeframes with average number of requests",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples =
                                    @ExampleObject(
                                            name = "example",
                                            description = "key: String -> value: Array[{String, Double}]",
                                            value =
                                                    "{ parkings/free: [{ 00:00 - 00:30: 0.4 }, {00:30 - 01:00: 0.32 }, ...], parkings: [{ 00:00 - 00:30: 0.31 }, {00:30 - 01:00: 0.26 }, ...] }")))
    @GetMapping(path = "/times", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<Map.Entry<String, Double>>>> getRequestStatsForTimes() {
        log.info("Fetching parking requests stats for times");
        Map<String, List<Map.Entry<String, Double>>> result = parkingRequestStatsService.getRequestStatsForTimes();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(
            summary = "get request peak times",
            description = "Returns the top 3 peak times based on average number of requests in a timeframe")
    @ApiResponse(
            responseCode = "200",
            description = "list of 3 peak times with average number of requests",
            content =
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Map.Entry.class)),
                            examples =
                                    @ExampleObject(
                                            name = "example",
                                            description = "Array[{String, Double}]",
                                            value =
                                                    "[{ 13:00 - 13:30: 23.0 }, { 18:30 - 19:00: 21.2 }, { 9:00 - 9:30: 18.4 }]")))
    @GetMapping(path = "/peak-times", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map.Entry<String, Double>>> getRequestPeakTimes() {
        log.info("Fetching peak request times");
        List<Map.Entry<String, Double>> result = parkingRequestStatsService.getRequestPeakTimes();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(
            summary = "get daily request statistics",
            description = "Returns the average number of requests per day for each endpoint")
    @ApiResponse(
            responseCode = "200",
            description = "map of daily statistics, key - endpoint name, value - average number of requests per day",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples =
                                    @ExampleObject(
                                            name = "example",
                                            description = "key: String -> value: Double",
                                            value = "{ parkings/free: 2.8, parkings/free/top: 2.5, parkings: 4.0 ")))
    @GetMapping(path = "/day", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Double>> getDailyRequestStats() {
        log.info("Fetching parking requests stats daily");
        Map<String, Double> result = parkingRequestStatsService.getDailyRequestStats();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
