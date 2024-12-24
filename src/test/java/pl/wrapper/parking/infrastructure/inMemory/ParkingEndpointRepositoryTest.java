package pl.wrapper.parking.infrastructure.inMemory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class ParkingEndpointRepositoryTest {

    private InMemoryRepository<Integer, DummyObject> inMemoryRepository;
    private Integer id;
    private DummyObject object;
    private final static String path = "data/statistics/tests";

    @BeforeEach
    void setUp() {
        inMemoryRepository = new ParkingEndpointRepository(path);
        object = new DummyObject();
        id = 10;
    }

    @AfterEach
    void tearDown() {
        File file = new File(path);
        assert file.delete();
    }

    @Test
    void shouldReturnObject(){
        inMemoryRepository.add(id,object);

        int first = inMemoryRepository.fetch().stream().findFirst().orElseThrow();

        assertEquals(first, id);

        assertEquals(inMemoryRepository.get(first), object);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSerializationCircleRunCorrectly() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        inMemoryRepository.add(id,object);

        Method selfSerialize = inMemoryRepository.getClass().getSuperclass().getDeclaredMethod("selfSerialize");
        selfSerialize.setAccessible(true);

        Method selfDeserialize = inMemoryRepository.getClass().getSuperclass().getDeclaredMethod("init");
        selfDeserialize.setAccessible(true);

        Field dataMap = inMemoryRepository.getClass().getSuperclass().getDeclaredField("dataMap");
        dataMap.setAccessible(true);
        Map<Integer, DummyObject> realMap = (Map<Integer, DummyObject>) dataMap.get(inMemoryRepository);

        selfSerialize.invoke(inMemoryRepository);
        realMap.clear();

        assertTrue(inMemoryRepository.fetch().isEmpty());

        selfDeserialize.invoke(inMemoryRepository);
        assertEquals(inMemoryRepository.get(id).s,object.s);
    }
}