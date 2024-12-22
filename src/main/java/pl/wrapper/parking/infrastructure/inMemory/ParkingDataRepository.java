package pl.wrapper.parking.infrastructure.inMemory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

@Component
public class ParkingDataRepository extends InMemoryRepositoryImpl<Integer, SomeObject> { //K and V types can/should be changed, but leave that for those who have the other two tasks

    public ParkingDataRepository(@Value("${serialization.location.parkingData}") String saveToLocationPath) {
        super(saveToLocationPath);
        this.defaultValue = null; //Add default value here (empty object probably), but leave that for those who have the other two tasks
    }

    @Override
    protected void init() {
        if (!file.exists()) {
            this.dataMap = new HashMap<>(); //put here whatever map type you want
            return;
        }

        try (FileInputStream fileOut = new FileInputStream(file);
             ObjectInputStream in = new ObjectInputStream(fileOut)) {

//            InMemoryRepoBasic<V> deserializedObject = (InMemoryRepoBasic<V>) in.readObject();
//            this.data = deserializedObject.data;
            //change it (we will serialize and read only the dataMap, not the whole object)
        } catch (IOException e) {
            throw new SerializationFailedException(createExceptionForIOE("Deserialization", e));
        }
    }
}
