package pl.wrapper.parking.infrastructure.error;

import org.springframework.http.HttpStatus;

public record ErrorWrapper(String errorMessage, HttpStatus expectedStatus, String Uri, HttpStatus occuredstatus) {}
