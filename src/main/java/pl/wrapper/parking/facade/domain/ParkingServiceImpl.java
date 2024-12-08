package pl.wrapper.parking.facade.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
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
    public Result<ParkingResponse> getByName(String name,@Nullable Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(null, null, name, opened);
        Optional<ParkingResponse> found = findParking(predicate);

        return found.map(this::handleFoundParking).orElseGet(() -> Result.failure(new ParkingError.ParkingNotFoundByName(name)));
    }

    @Override
    public Result<ParkingResponse> getById(Integer id,@Nullable Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(null, id, null, opened);
        Optional<ParkingResponse> found = findParking(predicate);

        return found.map(this::handleFoundParking).orElseGet(() -> Result.failure(new ParkingError.ParkingNotFoundById(id)));
    }

    @Override
    public Result<ParkingResponse> getBySymbol(String symbol,@Nullable Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(symbol, null, null, opened);
        Optional<ParkingResponse> found = findParking(predicate);

        return found.map(this::handleFoundParking).orElseGet(() -> Result.failure(new ParkingError.ParkingNotFoundBySymbol(symbol)));

    }

    @Override
    public List<ParkingResponse> getByParams(@Nullable String symbol, @Nullable Integer id, @Nullable String name, @Nullable Boolean opened) {
        Predicate<ParkingResponse> predicate = generatePredicateForParams(symbol, id, name, opened);

        return pwrApiServerCaller.fetchData().stream()
                .filter(predicate)
                .toList();
    }

    private Optional<ParkingResponse> findParking(Predicate<ParkingResponse> predicate){
        return pwrApiServerCaller.fetchData().stream()
                .filter(predicate)
                .findFirst();
    }


    private Result<ParkingResponse> handleFoundParking(ParkingResponse found){
        log.info("Parking found");
        return Result.success(found);
    }

    private Predicate<ParkingResponse> generatePredicateForParams(@Nullable String symbol,@Nullable Integer id ,@Nullable String name, @Nullable Boolean isOpened){
        Predicate<ParkingResponse> predicate = parking -> true;
        if (symbol != null)
            predicate = predicate.and(parking -> symbol.toLowerCase().contains(parking.symbol().toLowerCase()));
        if (id != null)
            predicate = predicate.and(parking -> Objects.equals(id, parking.parkingId()));
        if (name != null)
            predicate = predicate.and(parking -> name.toLowerCase().contains(parking.name().toLowerCase()));
        if (isOpened != null)
            predicate = predicate.and(parking -> Objects.equals(isOpened, parking.isOpened()));

        return predicate;
    }
}
