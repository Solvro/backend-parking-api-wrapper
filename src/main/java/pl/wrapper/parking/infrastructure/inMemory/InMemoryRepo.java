package pl.wrapper.parking.infrastructure.inMemory;

import java.io.Serializable;
import java.util.List;

public interface InMemoryRepo<T extends Serializable> extends Serializable {

    int add(T object);
    void deleteAllData();
    List<T> getState();
}
