package pl.wrapper.parking.facade.domain;

import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.infrastructure.error.ParkingError;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

@Service
@Slf4j
record ParkingServiceImpl(PwrApiServerCaller pwrApiServerCaller) implements ParkingService {

    @Override
    public Result<ParkingResponse> getByName(String name) {
        log.info("Received request: get parking by name: {}", name);
        Optional<ParkingResponse> found = pwrApiServerCaller.fetchData().stream()
                .filter(parking -> Objects.equals(name, parking.name()))
                .findFirst();

        log.info("All parkings has been checked for name");

        if (found.isPresent()) {
            log.info("Parking found for name: {}", name);
            return Result.success(found.get());
        }
        log.error("No parking found for name: {}", name);
        return Result.failure(new ParkingError.ParkingNotFoundByName(name));
    }

    @Override
    public Result<ParkingResponse> getById(Integer id) {
        log.info("Received request: get parking by id: {}", id);
        Optional<ParkingResponse> found = pwrApiServerCaller.fetchData().stream()
                .filter(parking -> Objects.equals(id, parking.parkingId()))
                .findFirst();

        log.info("All parkings has been checked for id");

        if (found.isPresent()) {
            log.info("Parking found for id: {}", id);
            return Result.success(found.get());
        }
        log.warn("No parking found for id: {}", id);
        return Result.failure(new ParkingError.ParkingNotFoundById(id));
    }

    @Override
    public Result<ParkingResponse> getBySymbol(String symbol) {
        log.info("Received request: get parking by symbol: {}", symbol);
        Optional<ParkingResponse> found = pwrApiServerCaller.fetchData().stream()
                .filter(parking -> Objects.equals(symbol, parking.symbol()))
                .findFirst();

        log.info("All parkings has been checked for symbol");

        if (found.isPresent()) {
            log.info("Parking found for symbol: {}", symbol);
            return Result.success(found.get());
        }
        log.warn("No parking found for symbol: {}", symbol);
        return Result.failure(new ParkingError.ParkingNotFoundBySymbol(symbol));
    }
}
