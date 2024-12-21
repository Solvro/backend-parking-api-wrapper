package pl.wrapper.parking.infrastructure.inMemory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
@Primary
public class InMemoryRepoBasic<T extends Serializable> implements InMemoryRepo<T> {
    @Serial
    private static final long serialVersionUID = 1L;
    protected final transient File file;
    protected List<T> data;

    public InMemoryRepoBasic(@Value("${serialization.location.basic}") String filePath) {
        this.data = new ArrayList<>();
        this.file = new File(filePath);
    }

    public int add(T object){
        data.add(object);
        return data.size() - 1;
    }

    public void deleteAllData(){
        data.clear();
    }

    public List<T> getState(){
        return data;
    }

    @Scheduled(fixedRateString = "#{60 * 100 * ${serialization.timeStamp.inMinutes}}")
    private void periodicSerialize(){
        selfSerialize();
    }

    @PreDestroy
    private void selfSerialize(){
        File parent = file.getParentFile();
        if (parent != null && !parent.exists())
            if (!parent.mkdirs())
                throw new SerializationFailedException("Failed to create directory for path: " + file.getAbsolutePath());


        try (FileOutputStream fileOut = new FileOutputStream(file);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {

            out.writeObject(this);
        } catch (IOException e) {
            throw new SerializationFailedException(createExceptionForIOE("Serialization",e));
            //should I catch it in GlobalExceptionHandel?
        }
    }

    @PostConstruct
    @SuppressWarnings("unchecked")
    private void selfDeserialize(){
        if (!file.exists()) {
            this.data = new ArrayList<>();
            return;
        }

        try (FileInputStream fileOut = new FileInputStream(file);
             ObjectInputStream in = new ObjectInputStream(fileOut)) {

            InMemoryRepoBasic<T> deserializedObject = (InMemoryRepoBasic<T>) in.readObject();
            this.data =  deserializedObject.data;

        } catch (IOException  | ClassNotFoundException e) {
            throw new SerializationFailedException(createExceptionForIOE("Deserialization",e));
            //should I catch it in GlobalExceptionHandel?
        }
    }

    private <E extends Exception> String createExceptionForIOE(String methodType , E e){
        return methodType + " failed for: " + this.getClass().getSimpleName() + ". Message: " + e.getMessage();
    }
}
