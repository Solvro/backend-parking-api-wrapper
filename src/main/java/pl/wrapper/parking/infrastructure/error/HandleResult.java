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

    @SneakyThrows
    protected final ResponseEntity<String> handleResult(Result<?> toHandle,
                                                HttpStatus onSuccess,
                                                HttpServletRequest request) {
        if(toHandle.isSuccess())
            return new ResponseEntity<>(ow.writeValueAsString(toHandle.getData()), onSuccess);

        String uri = request.getRequestURI();
        Error error = toHandle.getError();
        String errorMessage = getMessageByError(error);

        ErrorWrapper errorWrapper = new ErrorWrapper(errorMessage, onSuccess, uri);

        HttpStatus status = getStatusByError(error);
        return new ResponseEntity<>(ow.writeValueAsString(errorWrapper),status);
    }

    protected abstract HttpStatus getStatusByError(Error error);

    protected abstract String getMessageByError(Error error);
}
