package pl.wrapper.parking.facade;

import pl.wrapper.parking.infrastructure.error.Result;

import java.io.Serializable;
import java.util.List;

public interface SerializerService<T extends Serializable> {
    Result<Integer> add(T object);

    Result<List<T>> getAll();

    void deleteAll();
}