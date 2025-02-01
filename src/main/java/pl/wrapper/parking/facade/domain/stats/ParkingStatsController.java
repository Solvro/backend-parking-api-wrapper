package pl.wrapper.parking.facade.domain.stats;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.wrapper.parking.facade.ParkingStatsService;
import pl.wrapper.parking.facade.dto.stats.ParkingStatsResponse;
import pl.wrapper.parking.facade.dto.stats.daily.CollectiveDailyParkingStats;
import pl.wrapper.parking.facade.dto.stats.daily.DailyParkingStatsResponse;
import pl.wrapper.parking.facade.dto.stats.weekly.CollectiveWeeklyParkingStats;
import pl.wrapper.parking.facade.dto.stats.weekly.WeeklyParkingStatsResponse;
import pl.wrapper.parking.infrastructure.error.ErrorWrapper;
import pl.wrapper.parking.infrastructure.validation.validIds.ValidIds;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Parking API Stats", description = "Endpoints for parking statistics")
@RequestMapping("/stats")
class ParkingStatsController {

    private final ParkingStatsService parkingStatsService;

    @Operation(
            summary = "Get parking statistics",
            description = "Fetch statistics for specified parking IDs, day of week, and time",
            parameters = {
                    @Parameter(name = "ids", description = "List of parking IDs to filter stats for", example = "1,3,5"),
                    @Parameter(
                            name = "day_of_week",
                            description = "Day of week to filter stats for",
                            example = "WEDNESDAY"),
                    @Parameter(
                            name = "time",
                            description = "Time to filter stats for in ISO time format",
                            required = true,
                            example = "12:34:00")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Parking stats retrieved successfully",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                    @ArraySchema(
                                            schema =
                                            @Schema(implementation = ParkingStatsResponse.class)))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Incorrect or missing input query parameters",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorWrapper.class)))
            })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ParkingStatsResponse>> getParkingStats(
            @RequestParam(name = "ids", required = false) @ValidIds List<Integer> parkingIds,
            @RequestParam(name = "day_of_week", required = false) DayOfWeek dayOfWeek,
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) @RequestParam(name = "time") LocalTime time) {
        log.info(
                "Fetching parking stats with parameters: ids = {}, day_of_week = {}, time = {}",
                parkingIds,
                dayOfWeek,
                time);
        return ResponseEntity.ok(parkingStatsService.getParkingStats(parkingIds, dayOfWeek, time));
    }

    @Operation(
            summary = "Get daily parking statistics",
            description = "Fetch daily statistics for specified parking IDs and day of week",
            parameters = {
                    @Parameter(name = "ids", description = "List of parking IDs to filter stats for", example = "1,3,5"),
                    @Parameter(
                            name = "day_of_week",
                            description = "Day of week to filter stats for",
                            required = true,
                            example = "WEDNESDAY"),
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Daily parking stats retrieved successfully",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                    @ArraySchema(
                                            schema =
                                            @Schema(
                                                    implementation =
                                                            DailyParkingStatsResponse.class)))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Incorrect or missing input query parameters",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorWrapper.class)))
            })
    @GetMapping(path = "/daily", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DailyParkingStatsResponse>> getDailyParkingStats(
            @RequestParam(name = "ids", required = false) @ValidIds List<Integer> parkingIds,
            @RequestParam(name = "day_of_week") DayOfWeek dayOfWeek) {
        log.info("Fetching daily parking stats with parameters: ids = {}, day_of_week = {}", parkingIds, dayOfWeek);
        return ResponseEntity.ok(parkingStatsService.getDailyParkingStats(parkingIds, dayOfWeek));
    }

    @Operation(
            summary = "Get weekly parking statistics",
            description = "Fetch weekly statistics for specified parking IDs",
            parameters = {
                    @Parameter(name = "ids", description = "List of parking IDs to filter stats for", example = "1,3,5")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Weekly parking stats retrieved successfully",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                    @ArraySchema(
                                            schema =
                                            @Schema(
                                                    implementation =
                                                            WeeklyParkingStatsResponse.class)))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Incorrect input query parameters",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorWrapper.class)))
            })
    @GetMapping(path = "/weekly", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WeeklyParkingStatsResponse>> getWeeklyParkingStats(
            @RequestParam(name = "ids", required = false) @ValidIds List<Integer> parkingIds) {
        log.info("Fetching weekly parking stats with parameters: ids = {}", parkingIds);
        return ResponseEntity.ok(parkingStatsService.getWeeklyParkingStats(parkingIds));
    }

    @Operation(
            summary = "Get collective daily parking statistics",
            description = "Fetch daily statistics for specified parking IDs and day of week for each time interval",
            parameters = {
                    @Parameter(name = "ids", description = "List of parking IDs to filter stats for", example = "1,3,5"),
                    @Parameter(
                            name = "day_of_week",
                            description = "Day of week to filter stats for",
                            required = true,
                            example = "WEDNESDAY"),
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Collective daily parking stats retrieved successfully",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                    @ArraySchema(
                                            schema =
                                            @Schema(
                                                    implementation =
                                                            CollectiveDailyParkingStats.class)),
                                    examples =
                                    @ExampleObject(
                                            "[{\"parkingInfo\": {\"parkingId\": 3, \"totalSpots\": 54},"
                                                    + "\"statsMap\": {\"08:00:00\": {\"averageAvailability\": 0.723, \"averageFreeSpots\": 37},"
                                                    + "\"08:10:00\": {\"averageAvailability\": 0.69, \"averageFreeSpots\": 33}}}]"))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Incorrect or missing input query parameters",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorWrapper.class)))
            })
    @GetMapping(path = "/daily/collective", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CollectiveDailyParkingStats>> getCollectiveDailyParkingStats(
            @RequestParam(name = "ids", required = false) @ValidIds List<Integer> parkingIds,
            @RequestParam(name = "day_of_week") DayOfWeek dayOfWeek) {
        log.info(
                "Fetching collective daily parking stats with parameters: ids = {}, day_of_week = {}",
                parkingIds,
                dayOfWeek);
        return ResponseEntity.ok(parkingStatsService.getCollectiveDailyParkingStats(parkingIds, dayOfWeek));
    }

    @Operation(
            summary = "Get collective weekly parking statistics",
            description = "Fetch weekly statistics for specified parking IDs for each time interval of each day",
            parameters = {
                    @Parameter(name = "ids", description = "List of parking IDs to filter stats for", example = "1,3,5")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Collective weekly parking stats retrieved successfully",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                    @ArraySchema(
                                            schema =
                                            @Schema(
                                                    implementation =
                                                            CollectiveWeeklyParkingStats.class)))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Incorrect input query parameters",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorWrapper.class)))
            })
    @GetMapping(path = "/weekly/collective", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CollectiveWeeklyParkingStats>> getCollectiveWeeklyParkingStats(
            @RequestParam(name = "ids", required = false) @ValidIds List<Integer> parkingIds) {
        log.info("Fetching collective weekly parking stats with parameters: ids = {}", parkingIds);
        return ResponseEntity.ok(parkingStatsService.getCollectiveWeeklyParkingStats(parkingIds));
    }
}
