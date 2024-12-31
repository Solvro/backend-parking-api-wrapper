package pl.wrapper.parking.facade.domain;

import static pl.wrapper.parking.infrastructure.error.HandleResult.handleResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.facade.dto.ParkingStatsResponse;
import pl.wrapper.parking.infrastructure.error.ErrorWrapper;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

@RequestMapping("/parkings")
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Parking API", description = "API for managing parking-related operations")
public class ParkingController {
    private final ParkingService parkingService;

    @GetMapping(path = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getParkingStats(
            @RequestParam(name = "id", required = false) Integer parkingId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, fallbackPatterns = "yyyy-MM-dd HH:mm:ss")
                    @RequestParam(name = "start_timestamp", required = false)
                    LocalDateTime start,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, fallbackPatterns = "yyyy-MM-dd HH:mm:ss")
                    @RequestParam(name = "end_timestamp", required = false)
                    LocalDateTime end,
            HttpServletRequest request) {
        log.info(
                "Fetching parking stats with parameters: id = {}, start_timestamp = {}, end_timestamp = {}",
                parkingId,
                start,
                end);
        Result<ParkingStatsResponse> result = parkingService.getParkingStats(parkingId, start, end);
        return handleResult(result, HttpStatus.OK, request.getRequestURI());
    }

    @GetMapping(path = "/stats/date", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getParkingStats(
            @RequestParam(name = "id", required = false) Integer parkingId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(name = "start_date", required = false)
                    LocalDate start,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(name = "end_date", required = false)
                    LocalDate end,
            HttpServletRequest request) {
        log.info(
                "Fetching parking stats with parameters: id = {}, start_date = {}, end_date = {}",
                parkingId,
                start,
                end);
        Result<ParkingStatsResponse> result = parkingService.getParkingStats(
                parkingId, start != null ? start.atStartOfDay() : null, end != null ? end.atTime(23, 59, 59) : null);
        return handleResult(result, HttpStatus.OK, request.getRequestURI());
    }

    @GetMapping(path = "/stats/time", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getParkingStats(
            @RequestParam(name = "id", required = false) Integer parkingId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) @RequestParam(name = "start_time", required = false)
                    LocalTime start,
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) @RequestParam(name = "end_time", required = false)
                    LocalTime end,
            HttpServletRequest request) {
        log.info(
                "Fetching parking stats with parameters: id = {}, start_time = {}, end_time = {}",
                parkingId,
                start,
                end);
        Result<ParkingStatsResponse> result = parkingService.getParkingStats(parkingId, start, end);
        return handleResult(result, HttpStatus.OK, request.getRequestURI());
    }

    @Operation(summary = "get list of parking lots with free spots from all/opened/closed parking lots")
    @ApiResponse(
            responseCode = "200",
            description = "list of parking lots",
            content =
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ParkingResponse.class))))
    @GetMapping(path = "/free", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ParkingResponse>> getAllParkingWithFreeSpots(
            @Parameter(description = "search in opened parking lots") @RequestParam(required = false) Boolean opened) {
        log.info("Finding all parking with free spots");
        List<ParkingResponse> result = parkingService.getAllWithFreeSpots(opened);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "get parking with the most free spots from all/opened/closed parking lots")
    @ApiResponse(
            responseCode = "200",
            description = "parking found",
            content =
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ParkingResponse.class)))
    @ApiResponse(
            responseCode = "404",
            description = "parking not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorWrapper.class)))
    @GetMapping(path = "/free/top", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getParkingWithTheMostFreeSpots(
            @Parameter(description = "search in opened parking lots") @RequestParam(required = false) Boolean opened,
            HttpServletRequest request) {
        log.info("Finding parking with the most free spots");
        Result<ParkingResponse> result = parkingService.getWithTheMostFreeSpots(opened);
        return handleResult(result, HttpStatus.OK, request.getRequestURI());
    }

    @Operation(
            summary = "find the closest parking by address",
            parameters =
                    @Parameter(
                            name = "address",
                            description = "The address to find the closest parking for",
                            required = true,
                            example = "Flower 20, 50-337 Wroclaw"),
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Closest parking found",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ParkingResponse.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "No parking found for the given address",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ErrorWrapper.class)))
            })
    @GetMapping(path = "/address", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getClosestParking(
            @RequestParam("address") String address, HttpServletRequest request) {
        log.info("Finding closest parking for address: {}", address);
        Result<ParkingResponse> result = parkingService.getClosestParking(address);
        return handleResult(result, HttpStatus.OK, request.getRequestURI());
    }

    @Operation(summary = "get parking by name if opened")
    @ApiResponse(
            responseCode = "200",
            description = "parking found",
            content =
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ParkingResponse.class)))
    @ApiResponse(
            responseCode = "404",
            description = "parking not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorWrapper.class)))
    @GetMapping(path = "/name", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getParkingByName(
            @Parameter(description = "parking name") @RequestParam String name,
            @Parameter(description = "is parking opened") @RequestParam(required = false) Boolean opened,
            HttpServletRequest request) {
        log.info("Received request: get parking by name: {}", name);
        Result<ParkingResponse> result = parkingService.getByName(name, opened);
        return handleResult(result, HttpStatus.OK, request.getRequestURI());
    }

    @Operation(summary = "get parking by id if opened")
    @ApiResponse(
            responseCode = "200",
            description = "parking found",
            content =
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ParkingResponse.class)))
    @ApiResponse(
            responseCode = "404",
            description = "parking not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorWrapper.class)))
    @GetMapping(path = "/id", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getParkingById(
            @Parameter(description = "parking id") @RequestParam Integer id,
            @Parameter(description = "is parking opened") @RequestParam(required = false) Boolean opened,
            HttpServletRequest request) {
        log.info("Received request: get parking by id: {}", id);
        Result<ParkingResponse> result = parkingService.getById(id, opened);
        return handleResult(result, HttpStatus.OK, request.getRequestURI());
    }

    @Operation(summary = "get parking by symbol if opened")
    @ApiResponse(
            responseCode = "200",
            description = "parking found",
            content =
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ParkingResponse.class)))
    @ApiResponse(
            responseCode = "404",
            description = "parking not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorWrapper.class)))
    @GetMapping(path = "/symbol", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getParkingBySymbol(
            @Parameter(description = "parking symbol") @RequestParam String symbol,
            @Parameter(description = "is parking opened") @RequestParam(required = false) Boolean opened,
            HttpServletRequest request) {
        log.info("Received request: get parking by symbol: {}", symbol);
        Result<ParkingResponse> result = parkingService.getBySymbol(symbol, opened);
        return handleResult(result, HttpStatus.OK, request.getRequestURI());
    }

    @Operation(summary = "get list of parking lots by name, id, symbol, if opened and has free spots")
    @ApiResponse(
            responseCode = "200",
            description = "list with parking lots",
            content =
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ParkingResponse.class))))
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ParkingResponse>> getParkingByParams(
            @Parameter(description = "parking symbol") @RequestParam(required = false) String symbol,
            @Parameter(description = "parking id") @RequestParam(required = false) Integer id,
            @Parameter(description = "parking name") @RequestParam(required = false) String name,
            @Parameter(description = "is parking opened") @RequestParam(required = false) Boolean opened,
            @Parameter(description = "if parking has free spots") @RequestParam(required = false) Boolean freeSpots) {
        log.info(
                "Received request: get parking by symbol: {}, id: {}, name: {} and hasFreeSpots: {}",
                symbol,
                id,
                name,
                freeSpots);
        List<ParkingResponse> result = parkingService.getByParams(symbol, id, name, opened, freeSpots);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
