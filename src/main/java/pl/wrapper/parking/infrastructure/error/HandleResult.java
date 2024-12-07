package pl.wrapper.parking.infrastructure.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HandleResult {
    private static final ObjectWriter ow = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new JavaTimeModule())
            .writerWithDefaultPrettyPrinter();

    @SneakyThrows
    public static ResponseEntity<String> handleResult(
            Result<?> toHandle, HttpStatus onSuccess, String uri) {
        if (toHandle.isSuccess())
            return new ResponseEntity<>(ow.writeValueAsString(toHandle.getData()), onSuccess);
        Error error = toHandle.getError();
        ErrorWrapper errorWrapper = getInfoByError(error,uri,onSuccess);
        return new ResponseEntity<>(ow.writeValueAsString(errorWrapper), errorWrapper.occuredstatus());
    }

    private static ErrorWrapper getInfoByError(Error error, String uri, HttpStatus onSuccess) {
        return switch (error) {
            case ParkingError.ParkingNotFoundBySymbol e -> new ErrorWrapper(
                    "Wrong Parking Symbol: " + e.symbol(), onSuccess, uri, HttpStatus.NOT_FOUND);
            case ParkingError.ParkingNotFoundById e -> new ErrorWrapper(
                    "Wrong Parking Id: " + e.id(), onSuccess, uri, HttpStatus.NOT_FOUND);
        };
    }}
