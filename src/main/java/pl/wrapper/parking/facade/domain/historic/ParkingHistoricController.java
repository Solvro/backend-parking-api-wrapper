package pl.wrapper.parking.facade.domain.historic;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wrapper.parking.facade.ParkingHistoricDataService;
import pl.wrapper.parking.facade.dto.historicData.HistoricDayParkingData;
import pl.wrapper.parking.facade.dto.historicData.HistoricPeriodParkingData;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@Tag(name = "Parking API Historic", description = "Endpoints for accessing historic data. Free spots with value=-1 mean that data couldn't have been fetched at that time. Missing/null days in periods mean the same.")
@RequestMapping("/historic")
class ParkingHistoricController {

    private ParkingHistoricDataService parkingHistoricDataService;


    @Operation(
            summary = "Get historic data for the given day and parking of given id",
            parameters = {
                    @Parameter(
                            name = "forDay",
                            description = "Date in ISO date format",
                            required = true,
                            example = "2025-07-29")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Historic data retrieved successfully",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = HistoricDayParkingData.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No data for the given parking lot for the given date")
            })
    @GetMapping(path = "/day/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HistoricDayParkingData> getHistoricDataForDayAndId(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("forDay") LocalDate forDay,
            @PathVariable(name = "id") @Min(1) @Max(5) Integer parkingId){
        HistoricDayParkingData dataForDay = parkingHistoricDataService.getDataForDay(forDay, parkingId);
        if(dataForDay == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dataForDay);
    }

    @Operation(
            summary = "Get historic data for the given day for all parking lots",
            parameters = {
                    @Parameter(
                            name = "forDay",
                            description = "Date in ISO date format",
                            required = true,
                            example = "2025-07-29")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Historic data retrieved successfully",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                    @ArraySchema(
                                            schema =
                                            @Schema(implementation = HistoricDayParkingData.class)))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No data for the given date"
                    )
            })
    @GetMapping(path = "/day", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HistoricDayParkingData>> getHistoricDataForDay(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("forDay") LocalDate forDay){
        List<HistoricDayParkingData> dataForDay = parkingHistoricDataService.getDataForDay(forDay);
        if(dataForDay == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dataForDay);
    }

    @Operation(
            summary = "Get all available historic data for the given period for the given id, both ends inclusive. If no end data given, get until today.",
            parameters = {
                    @Parameter(
                            name = "fromDate",
                            description = "Date in ISO date format",
                            required = true,
                            example = "2025-07-29"),
                    @Parameter(
                            name = "toDate",
                            description = "Date in ISO date format",
                            required = false,
                            example = "2025-08-29")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Historic data retrieved successfully",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = HistoricPeriodParkingData.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No data for the given parking lot for the given period")
            })
    @GetMapping(path = "/period/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HistoricPeriodParkingData> getHistoricDataForPeriodAndId(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("fromDate") LocalDate fromDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate,
            @PathVariable(name = "id") @Min(1) @Max(5) Integer parkingId){
        HistoricPeriodParkingData dataForPeriod = parkingHistoricDataService.getDataForPeriod(fromDate, toDate, parkingId);
        if(dataForPeriod == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dataForPeriod);
    }

    @Operation(
            summary = "Get all available historic data for the given period for all parking lots, both ends inclusive. If no end data given, get until today.",
            parameters = {
                    @Parameter(
                            name = "fromDate",
                            description = "Date in ISO date format",
                            required = true,
                            example = "2025-07-29"),
                    @Parameter(
                            name = "toDate",
                            description = "Date in ISO date format",
                            required = false,
                            example = "2025-08-29")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Historic data retrieved successfully",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array =
                                    @ArraySchema(
                                            schema =
                                            @Schema(implementation = HistoricPeriodParkingData.class)))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No data for the given period"
                    )
            })
    @GetMapping(path = "/period", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HistoricPeriodParkingData>> getHistoricDataForPeriod(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("fromDate") LocalDate fromDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate){
        List<HistoricPeriodParkingData> dataForPeriod = parkingHistoricDataService.getDataForPeriod(fromDate, toDate);
        if(dataForPeriod == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dataForPeriod);
    }

}
