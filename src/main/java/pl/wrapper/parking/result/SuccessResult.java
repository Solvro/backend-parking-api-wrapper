package pl.wrapper.parking.result;

public record SuccessResult<T>(T result) implements Result<T>{

    @Override
    public T getValue() {
        return result;
    }

    @Override
    public Boolean isSuccess() {
        return true;
    }
}