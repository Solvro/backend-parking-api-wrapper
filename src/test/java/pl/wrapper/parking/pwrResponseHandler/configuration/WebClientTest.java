package pl.wrapper.parking.pwrResponseHandler.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import pl.wrapper.parking.infrastructure.exception.PwrApiNotRespondingException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;
import static pl.wrapper.parking.pwrResponseHandler.configuration.WebClientConfig.buildRetryFilter;

class WebClientTest {

    @Test
    void testResponseFilterAndRetryFilterIntegration() {
        ClientResponse forbiddenResponse = ClientResponse.create(HttpStatus.FORBIDDEN)
                .body("Access Denied")
                .build();
        ExchangeFunction mockExchangeFunction = mock(ExchangeFunction.class);
        when(mockExchangeFunction.exchange(any()))
                .thenReturn(Mono.just(forbiddenResponse));
        WebClient webClientWithFilters = WebClient.builder()
                .filter(buildRetryFilter())
                .filter(ExchangeFilterFunction.ofResponseProcessor(WebClientConfig::responseFilter))
                .exchangeFunction(mockExchangeFunction)
                .build();

        Mono<String> response = webClientWithFilters.get()
                .uri("/mock-uri")
                .retrieve()
                .bodyToMono(String.class);

        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof PwrApiNotRespondingException
                        && throwable.getMessage().equals("Access Denied"))
                .verify();
        verify(mockExchangeFunction, times(4)).exchange(any());
    }

}
