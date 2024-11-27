package pl.wrapper.parking.infrastructure.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public record ErrorWrapper(String errorMessage,
                           HttpStatus expectedStatus,
                           String URI) {}