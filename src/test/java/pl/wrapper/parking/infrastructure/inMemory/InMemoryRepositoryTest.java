package pl.wrapper.parking.infrastructure.inMemory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRepositoryTest {

    private InMemoryRepositoryTestImpl inMemoryRepository;
    private Integer id;
    private String value = "value";
    private static final String path = "data/statistics/tests";

    static class InMemoryRepositoryTestImpl extends InMemoryRepositoryImpl<Integer, String> {
        
        public InMemoryRepositoryTestImpl(String filePath, Map<Integer, String> map, String defaultValue) {
            super(filePath, map, defaultValue);
        }

        public void testSerialize() {
            periodicSerialize();
        }

        public void testDeserialize() {
            init();
        }

        public void deleteData() {
            dataMap.clear();
        }
    }

    @BeforeEach
    void setUp() {
        inMemoryRepository = new InMemoryRepositoryTestImpl(path, new HashMap<>(), null);
        value = new String("value");
        id = 10;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterEach
    void tearDown() {
        File file = new File(path);
        file.delete();
    }

    @Test
    void shouldReturnObject() {
        inMemoryRepository.add(id, value);

        int first = inMemoryRepository.fetchAllKeys().stream().findFirst().orElseThrow();

        assertEquals(first, id);
        assertEquals(inMemoryRepository.get(first), value);
    }

    @Test
    void shouldSerializationRunCorrectly() {
        inMemoryRepository.add(id, value);

        inMemoryRepository.testSerialize();
        inMemoryRepository.deleteData();

        assertTrue(inMemoryRepository.fetchAllKeys().isEmpty());

        inMemoryRepository.testDeserialize();
        assertEquals(inMemoryRepository.get(id), value);
    }
}
