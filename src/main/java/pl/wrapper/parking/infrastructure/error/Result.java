package pl.wrapper.parking.infrastructure.error;

import pl.wrapper.parking.infrastructure.exception.InvalidCallException;

public interface Result<T> {
    static <T> Result<T> success(T data) {
        return new Success<>(data);
    }

    static <T> Failure<T> failure(Error error) {
        return new Failure<>(error);
    }

    boolean isSuccess();

    T getData();

    Error getError();

    record Success<T>(T data) implements Result<T> {
        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public T getData() {
            return data;
        }

        @Override
        public Error getError() {
            throw new InvalidCallException("Call getError() on Success");
        }
    }

    record Failure<T>(Error error) implements Result<T> {
        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public T getData() {
            throw new InvalidCallException("Call getData() on Failure");
        }

        @Override
        public Error getError() {
            return error;
        }
    }
}
