package pl.wrapper.parking.InMemory;

import org.springframework.stereotype.Service;
import pl.wrapper.parking.facade.SerializerService;
import pl.wrapper.parking.infrastructure.error.Result;
import pl.wrapper.parking.infrastructure.inMemory.InMemoryRepo;

import java.util.List;

@Service
public record InMemoryServiceTest(InMemoryRepo<InMemoryObjectTest> repo) implements SerializerService<InMemoryObjectTest> {
    @Override
    public Result<Integer> add(InMemoryObjectTest object) {
        return Result.success(repo.add(object));
    }

    @Override
    public Result<List<InMemoryObjectTest>> getAll() {
        return Result.success(repo.getState());
    }

    @Override
    public void deleteAll() {
        repo.deleteAllData();
    }
}
