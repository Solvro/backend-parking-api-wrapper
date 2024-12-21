package pl.wrapper.parking.InMemory;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class InMemoryTest {

    @Autowired
    private InMemoryServiceTest serviceTest;


    @Test
    public void shouldReturnData() {
        InMemoryObjectTest test = new InMemoryObjectTest();
        serviceTest.deleteAll();

        serviceTest.add(test);

        assertEquals(serviceTest.getAll().getData().getFirst(),test);

        serviceTest.deleteAll();

        assertTrue(serviceTest.getAll().getData().isEmpty());

        serviceTest.add(test);
        log.info("check place for serialization");
    }
}
