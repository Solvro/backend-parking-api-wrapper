package pl.wrapper.parking.facade.domain;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.infrastructure.error.ErrorWrapper;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

import java.util.List;

import static pl.wrapper.parking.infrastructure.error.HandleResult.*;

@RequestMapping("/v1/parkings")
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Parking API")
class ParkingController {
    private final ParkingService parkingService;

    @GetMapping
    public ResponseEntity<String> getAllParkings(HttpServletRequest request) {
        log.info("Fetching all parking lots.");
        return handleResult(parkingService.getAllParkings(), HttpStatus.OK, request.getRequestURI());
    }

    @GetMapping(params = "address")
    public ResponseEntity<String> getClosestParking(@RequestParam("address") String address, HttpServletRequest request) {
        log.info("Finding closest parking for address: {}", address);
        return handleResult(parkingService.getClosestParking(address), HttpStatus.OK, request.getRequestURI());
    }

    @Value("${server.servlet.context-path}")
    private String apiPath;

    @Operation(summary = "get parking by name if opened")
    @ApiResponse(responseCode = "200", description = "parking found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ParkingResponse.class)))
    @ApiResponse(responseCode = "404", description = "parking not found",content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorWrapper.class)))
    @GetMapping("/name")
    public ResponseEntity<String> getParkingByName(@Parameter(description = "parking name") @RequestParam String name,@Parameter(description = "is parking opened") @RequestParam(required = false) Boolean opened) {
        log.info("Received request: get parking by name: {}", name);
        Result<ParkingResponse> result = parkingService.getByName(name, opened);

        return handleResult(result, HttpStatus.OK,  apiPath + "/name");
    }

    @Operation(summary = "get parking by id if opened")
    @ApiResponse(responseCode = "200", description = "parking found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ParkingResponse.class)))
    @ApiResponse(responseCode = "404", description = "parking not found",content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorWrapper.class)))
    @GetMapping("/id")
    public ResponseEntity<String> getParkingById(@Parameter(description = "parking id") @RequestParam Integer id,@Parameter(description = "is parking opened") @RequestParam(required = false) Boolean opened) {
        log.info("Received request: get parking by id: {}", id);
        Result<ParkingResponse> result = parkingService.getById(id, opened);
        return handleResult(result, HttpStatus.OK, apiPath + "/id");
    }

    @Operation(summary = "get parking by symbol if opened")
    @ApiResponse(responseCode = "200", description = "parking found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ParkingResponse.class)))
    @ApiResponse(responseCode = "404", description = "parking not found",content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorWrapper.class)))
    @GetMapping("/symbol")
    public ResponseEntity<String> getParkingBySymbol(@Parameter(description = "parking symbol") @RequestParam String symbol,@Parameter(description = "is parking opened") @RequestParam(required = false) Boolean opened) {
        log.info("Received request: get parking by symbol: {}", symbol);
        Result<ParkingResponse> result = parkingService.getBySymbol(symbol, opened);
        return handleResult(result, HttpStatus.OK,  apiPath + "/symbol");
    }

    @Operation(summary = "get list of parkings by name, id, symbol and if opened")
    @ApiResponse(responseCode = "200", description = "list with parkings", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ParkingResponse.class))))
    @GetMapping
    public ResponseEntity<List<ParkingResponse>> getParkingByParams(@Parameter(description = "parking symbol") @RequestParam(required = false) String symbol,
                                                                    @Parameter(description = "parking id") @RequestParam(required = false) Integer id,
                                                                    @Parameter(description = "parking name") @RequestParam(required = false) String name,
                                                                    @Parameter(description = "is parking opened") @RequestParam(required = false) Boolean opened) {
        log.info("Received request: get parking by symbol: {},id: {} and name: {}",symbol,id,name);
        List<ParkingResponse> result = parkingService.getByParams(symbol, id, name, opened);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
