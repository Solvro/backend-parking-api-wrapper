package pl.wrapper.parking.result;


import lombok.SneakyThrows;

public record FailureResult<T,D extends Exception>(D error) implements Result<T> {

    public static <M> FailureResult<M,? extends Exception> failureResult(Result<?> other) {
        if (other instanceof FailureResult<?,?> failure)
            return new FailureResult<>(failure.error);

        return new FailureResult<>(new IllegalArgumentException("Provided Result is not a FailureResult"));
    }

    @SneakyThrows
    @Override
    public T getValue(){
        throw error;
    }

    @Override
    public Boolean isSuccess() {
        return false;
    }
}