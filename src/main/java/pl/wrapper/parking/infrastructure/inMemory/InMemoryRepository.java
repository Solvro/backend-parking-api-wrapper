package pl.wrapper.parking.infrastructure.inMemory;

import java.io.Serializable;
import java.util.Set;

public interface InMemoryRepository<K extends Serializable, V extends Serializable> {
    void add(K key, V value);
    V get(K key);
    Set<K> fetch();
}
