package pl.wrapper.parking.infrastructure.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class HandleResult {
    private final ObjectWriter ow = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new JavaTimeModule())
            .writerWithDefaultPrettyPrinter();

    @SneakyThrows
    protected final ResponseEntity<String> handleResult(
            Result<?> toHandle, HttpStatus onSuccess, String uri) {
        if (toHandle.isSuccess()) return new ResponseEntity<>(ow.writeValueAsString(toHandle.getData()), onSuccess);

        Error error = toHandle.getError();
        ErrorWrapper errorWrapper = getInfoByError(error,uri,onSuccess);
        return new ResponseEntity<>(ow.writeValueAsString(errorWrapper), errorWrapper.Occuredstatus());
    }

    protected abstract ErrorWrapper getInfoByError(Error error, String uri, HttpStatus onSuccess);
}
