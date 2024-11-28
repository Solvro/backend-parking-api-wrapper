package pl.wrapper.parking.infrastructure.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class HandleResult {
    private final ObjectWriter ow = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new JavaTimeModule())
            .writerWithDefaultPrettyPrinter();

    public record Pair<T,D>(T first, D second) {}


    @SneakyThrows
    protected final ResponseEntity<String> handleResult(Result<?> toHandle,
                                                HttpStatus onSuccess,
                                                HttpServletRequest request) {
        if(toHandle.isSuccess())
            return new ResponseEntity<>(ow.writeValueAsString(toHandle.getData()), onSuccess);

        String uri = request.getRequestURI();
        Error error = toHandle.getError();

        Pair<HttpStatus, String> info = getInfoByError(error);
        String errorMessage = info.second;
        HttpStatus status = info.first;

        ErrorWrapper errorWrapper = new ErrorWrapper(errorMessage, onSuccess, uri);

        return new ResponseEntity<>(ow.writeValueAsString(errorWrapper),status);
    }

    protected abstract Pair<HttpStatus, String> getInfoByError(Error error);
}