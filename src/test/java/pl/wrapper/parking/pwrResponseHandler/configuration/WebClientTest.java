package pl.wrapper.parking.pwrResponseHandler.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static pl.wrapper.parking.pwrResponseHandler.configuration.WebClientConfig.buildRetryFilter;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import pl.wrapper.parking.infrastructure.exception.PwrApiNotRespondingException;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;
import pl.wrapper.parking.pwrResponseHandler.domain.PwrApiCaller;
import pl.wrapper.parking.pwrResponseHandler.domain.PwrApiServerCallerImpl;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class WebClientTest {

    @Test
    void testResponseFilterAndRetryFilterIntegration() {
        ClientResponse forbiddenResponse = ClientResponse.create(HttpStatus.FORBIDDEN)
                .body("Access Denied")
                .build();
        ExchangeFunction mockExchangeFunction = mock(ExchangeFunction.class);
        when(mockExchangeFunction.exchange(any())).thenReturn(Mono.just(forbiddenResponse));
        WebClient webClientWithFilters = WebClient.builder()
                .filter(buildRetryFilter())
                .filter(ExchangeFilterFunction.ofResponseProcessor(WebClientConfig::responseFilter))
                .exchangeFunction(mockExchangeFunction)
                .build();

        Mono<String> response =
                webClientWithFilters.get().uri("/mock-uri").retrieve().bodyToMono(String.class);

        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof PwrApiNotRespondingException
                        && throwable.getMessage().equals("Access Denied"))
                .verify();
        verify(mockExchangeFunction, times(4)).exchange(any());
    }

    @Test
    void shouldReturnException() {
        Exception provided = new ClassCastException("simulated");

        PwrApiCaller apiCaller = Mockito.mock(PwrApiCaller.class);
        Mockito.when(apiCaller.fetchParkingPlaces()).thenReturn(Mono.error(provided));

        PwrApiServerCaller pwrApiServerCaller = new PwrApiServerCallerImpl(apiCaller);
        Exception e = assertThrows(provided.getClass(), pwrApiServerCaller::fetchParkingData);

        assertEquals(provided.getMessage(), e.getMessage());
    }
}
