package pl.wrapper.parking.result;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.wrapper.parking.exception.ExceptionStatus;

@JsonIgnoreProperties({"value"})
public record FailureResult<T,D extends Exception>(
        @JsonIgnoreProperties({"stackTrace"}) D error) implements Result<T> {

    public static <M> FailureResult<M,? extends Exception> failureResult(Result<?> other) {
        if (other instanceof FailureResult<?,?> failure)
            return new FailureResult<>(failure.error);

        return new FailureResult<>(new IllegalArgumentException("Provided Result is not a FailureResult"));
    }

    @SneakyThrows
    @Override
    public T getValue(){
        throw error;
    }

    @Override
    public Boolean isSuccess() {
        return false;
    }

    @JsonGetter
    private String getErrorType(){
        return error.getClass().getSimpleName();
    }

    @Override
    public ResponseEntity<Result<T>> getResponseEntity(HttpStatus httpStatus){
        return new ResponseEntity<>(this, ExceptionStatus.getStatusForException(error.getClass()));
    }
}