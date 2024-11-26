package pl.wrapper.parking.infrastructure.error;

import pl.wrapper.parking.infrastructure.exception.InvalidCallException;

public interface Result<T> {
    static <T> Result<T> success(T data) {return new Success<>(data); }
    static <T> Failure<T> failure(Error error) {return new Failure<>(error); }

    boolean isSuccess();
    boolean isFailure();
    T getData();
    Error getError();


    public static final class Success<T> implements Result<T> {
        private final T data;
        private Success(T data) { this.data = data;}

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
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

    public static final class Failure<T> implements Result<T> {
        private final Error error;
        private Failure(Error error) { this.error = error;}

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
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
