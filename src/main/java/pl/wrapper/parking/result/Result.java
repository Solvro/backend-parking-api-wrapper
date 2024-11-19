package pl.wrapper.parking.result;

import lombok.Getter;
import lombok.Setter;

/**
 * Class {@code Result} is a wrapper for any returning value and
 * can storage any exception as its type and error description.
 *<p>
 * {@code Result} is containing 2 static methods for generating
 * a correct {@code Result} instance:
 * <ul>
 * <li>{@code Success}: generate the success instance</li>
 * <li>{@code Failure}: generate the failure instance</li>
 * </ul>
 *
 * @param <T> the type of the result
 */
@Getter
@Setter
public class Result<T> {
    private T result;
    private boolean success;
    private ErrorResult error;

    private Result(T result,boolean success, ErrorResult error){
        this.result = result;
        this.success = success;
        this.error = error;
    }
    /**
     * Static method that create the {@code Result} instance with the provided result
     *
     * @param result the value of the successful result
     * @param <T> the type of the result
     * @return {@code Result} instance representing a success with the provided result value
     */
    public static <T> Result<T> Success(T result){
        return new Result<>(result,true,null);
    }
    /**
     * Static method that create the {@code Result} instance with the provided exception
     *
     * @param exception the class of the shown exception
     * @param message the string of the error description
     * @param <T> the type of the result
     * @return instance representing a failure with the provided exception and description
     */
    public static <T> Result<T> Failure(Class<? extends Exception> exception, String message){
        return new Result<>(null,false,new ErrorResult(message,exception.getName()));
    }
}
