package pl.wrapper.parking.result;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record SuccessResult<T>(T result) implements Result<T>{

    @Override
    public T getValue() {
        return result;
    }

    @Override
    public Boolean isSuccess() {
        return true;
    }

    @Override
    public ResponseEntity<Result<T>> getResponseEntity(HttpStatus httpStatus) {
        return new ResponseEntity<>(this, httpStatus);
    }
}