package pl.wrapper.parking.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Class {@code ErrorResult} represent exceptions.
 * It contains error description and error code as Strings.
 */
@Getter
@Setter
@AllArgsConstructor
public class ErrorResult {
    private String errorMessage;
    private String errorCode;
}
