package pl.wrapper.parking.facade.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pl.wrapper.parking.infrastructure.error.ParkingError;
import pl.wrapper.parking.infrastructure.error.Result;

@RestController
@RequiredArgsConstructor
public class DummyController {
    private final DummyService dummyService;

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<String> getParkingOccupancyByParkingIdValid(@PathVariable("symbol") String symbol) {
        boolean willSucceed = true;
        return handleResult(dummyService.dummyGetParkingBySymbol(symbol, willSucceed), HttpStatus.OK);
    }


    //-------
    private static final ObjectWriter ow = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new JavaTimeModule())
            .writerWithDefaultPrettyPrinter();

    //ErrorWrapper -> @JsonProperty error -> body...
    //$body.error -> body
    @SneakyThrows
    private static ResponseEntity<String> handleResult(Result<?> toHandle, HttpStatus onSuccess) {
        if(toHandle.isSuccess()) return new ResponseEntity<>(ow.writeValueAsString(toHandle), onSuccess);
        return switch (toHandle.getError()){
            case ParkingError.ParkingNotFoundBySymbol e -> new ResponseEntity<>("Custom error message here", HttpStatus.NOT_FOUND);
            //other cases here
        };
    }

}