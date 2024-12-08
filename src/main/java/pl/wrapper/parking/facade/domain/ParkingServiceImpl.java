package pl.wrapper.parking.facade.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

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
    public Result<ParkingResponse> getByName(String name, Boolean opened) {
        Optional<ParkingResponse> found = findParking(parking -> Objects.equals(name, parking.name()),opened);
        log.info("All parkings has been checked for name");

        if (found.isPresent()) return handleFoundParking(found.get());

        log.info("No parking found for name: {}", name);
        return Result.failure(new ParkingError.ParkingNotFoundByName(name));
    }

    @Override
    public Result<ParkingResponse> getById(Integer id, Boolean opened) {
        Optional<ParkingResponse> found = findParking(parking -> Objects.equals(id, parking.parkingId()),opened);
        log.info("All parkings has been checked for id");

        if (found.isPresent()) return handleFoundParking(found.get());

        log.info("No parking found for id: {}", id);
        return Result.failure(new ParkingError.ParkingNotFoundById(id));
    }

    @Override
    public Result<ParkingResponse> getBySymbol(String symbol, Boolean opened) {
        Optional<ParkingResponse> found = findParking(parking -> Objects.equals(symbol, parking.symbol()),opened);
        log.info("All parkings has been checked for symbol");

        if (found.isPresent()) return handleFoundParking(found.get());

        log.info("No parking found for symbol: {}", symbol);
        return Result.failure(new ParkingError.ParkingNotFoundBySymbol(symbol));
    }

    @Override
    public Result<List<ParkingResponse>> getByParams(String symbol, Integer id, String name, Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(symbol, id, name);

        List<ParkingResponse> list = pwrApiServerCaller.fetchData().stream()
                .filter(predicate)
                .filter(parking -> handleIsOpened(opened, parking.isOpened()))
                .toList();

        log.info("All parkings has been checked for all params");
        return Result.success(list);
    }

    private Optional<ParkingResponse> findParking(Predicate<ParkingResponse> predicate, Boolean opened){
        return pwrApiServerCaller.fetchData().stream()
                .filter(predicate)
                .filter(parking -> handleIsOpened(opened, parking.isOpened()))
                .findFirst();
    }

    private boolean handleIsOpened(Boolean opened, Boolean isParkingOpened){
        if (opened == null) return true;
        return opened == isParkingOpened;
    }

    private Result<ParkingResponse> handleFoundParking(ParkingResponse found){
        log.info("Parking found");
        return Result.success(found);
    }

    private Predicate<ParkingResponse> generatePredicateForParams(String symbol,Integer id ,String name){
        Predicate<ParkingResponse> predicate = parking -> true;
        if (symbol != null)
            predicate = predicate.and(parking -> Objects.equals(symbol, parking.symbol()));
        if (id != null)
            predicate = predicate.and(parking -> Objects.equals(id, parking.parkingId()));
        if (name != null)
            predicate = predicate.and(parking -> Objects.equals(name, parking.name()));
        log.info("Predicate has been created for all existing params");

        return predicate;
    }
}
