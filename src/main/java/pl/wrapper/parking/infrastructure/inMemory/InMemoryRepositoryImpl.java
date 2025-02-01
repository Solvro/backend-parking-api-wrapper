package pl.wrapper.parking.infrastructure.inMemory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class InMemoryRepositoryImpl<K extends Serializable, V extends Serializable>
        implements InMemoryRepository<K, V> {

    protected final transient File file;
    protected Map<K, V> dataMap;
    protected final V defaultValue;

    public InMemoryRepositoryImpl(String filePath, Map<K, V> map, V defaultValue) {
        this.file = new File(filePath);
        this.defaultValue = defaultValue;

        this.dataMap = map;
    }

    @Override
    public void add(K key, V value) {
        dataMap.put(key, value);
    }

    @Override
    public Set<K> fetchAllKeys() {
        return Collections.unmodifiableSet(dataMap.keySet());
    }

    @Override
    public Set<Map.Entry<K, V>> fetchAllEntries() {
        return dataMap.entrySet();
    }

    @Override
    public V get(K key) {
        return dataMap.getOrDefault(key, defaultValue);
    }

    @PostConstruct
    @SuppressWarnings("unchecked")
    protected void init() {
        if (!file.exists()) return;

        try (FileInputStream fileIn = new FileInputStream(file);
                ObjectInputStream in = new ObjectInputStream(fileIn)) {

            this.dataMap = (Map<K, V>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationFailedException(createExceptionForIOE("Deserialization", e));
        }
    }

    @PreDestroy
    private void selfSerialize() {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists())
            if (!parent.mkdirs())
                throw new SerializationFailedException(
                        "Failed to create directory for path: " + file.getAbsolutePath());

        try (FileOutputStream fileOut = new FileOutputStream(file);
                ObjectOutputStream out = new ObjectOutputStream(fileOut)) {

            out.writeObject(dataMap);
        } catch (IOException e) {
            throw new SerializationFailedException(createExceptionForIOE("Serialization", e));
        }
    }

    @Scheduled(fixedRateString = "#{60 * 1000 * ${serialization.timeStamp.inMinutes}}", initialDelay = 10 * 1000)
    protected void periodicSerialize() {
        selfSerialize();
    }

    private static <E extends Exception> String createExceptionForIOE(String methodType, E e) {
        return methodType + " failed for: " + InMemoryRepositoryImpl.class.getSimpleName() + ". Message: "
                + e.getMessage();
    }
}
