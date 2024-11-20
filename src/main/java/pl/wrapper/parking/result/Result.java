package pl.wrapper.parking.result;


import lombok.SneakyThrows;

public interface Result<T> {
    T getValue();
    Boolean isSuccess();
}