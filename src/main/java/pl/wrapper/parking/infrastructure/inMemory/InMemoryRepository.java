package pl.wrapper.parking.infrastructure.inMemory;

import java.io.Serializable;

public interface InMemoryRepository<K extends Serializable, V extends Serializable> {

    void add(K key, V value);
    V get(K key);
    //add a method to fetch all k,v pairs from dataMap
}
