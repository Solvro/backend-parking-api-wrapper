package pl.wrapper.parking.result;


import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public interface Result<T> {
    T getValue();
    Boolean isSuccess();
    ResponseEntity<Result<T>> getResponseEntity(HttpStatus httpStatus);
}