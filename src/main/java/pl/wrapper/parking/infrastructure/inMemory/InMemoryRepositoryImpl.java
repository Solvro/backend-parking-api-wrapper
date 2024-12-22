package pl.wrapper.parking.infrastructure.inMemory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.*;
import java.util.Map;

public abstract class InMemoryRepositoryImpl<K extends Serializable, V extends Serializable> implements InMemoryRepository<K, V> {
    protected final transient File file;
    protected Map<K, V> dataMap;
    protected V defaultValue;

    public InMemoryRepositoryImpl(String filePath) {
        this.file = new File(filePath);
    }

    @PostConstruct
    protected abstract void init();

    @PreDestroy
    private void selfSerialize() {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists())
            if (!parent.mkdirs())
                throw new SerializationFailedException("Failed to create directory for path: " + file.getAbsolutePath());
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            //do not serialize whole, let's serialize only the dataMap. Should be far easier that way
        } catch (IOException e) {
            throw new SerializationFailedException(createExceptionForIOE("Serialization", e));
        }
        //Add handling for these exceptions in exceptionHandler
        //remember to close in/out streams (not necessary but still)
    }

    @Scheduled(fixedRateString = "#{60 * 100 * ${serialization.timeStamp.inMinutes}}")
    private void periodicSerialize() {
        selfSerialize();
    }

    protected  <E extends Exception> String createExceptionForIOE(String methodType, E e) {
        //make it static
        return methodType + " failed for: " + this.getClass().getSimpleName() + ". Message: " + e.getMessage();
    }

    @Override
    public void add(K key, V value) {
        //add here
    }

    //add method to fetch all k,v pairs from dataMap

    @Override
    public V get(K key) {
        return dataMap.getOrDefault(key, defaultValue);
    }
}
