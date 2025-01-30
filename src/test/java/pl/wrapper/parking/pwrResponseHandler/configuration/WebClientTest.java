package pl.wrapper.parking.pwrResponseHandler.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import pl.wrapper.parking.infrastructure.exception.PwrApiNotRespondingException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.Mockito.*;
import static pl.wrapper.parking.pwrResponseHandler.configuration.WebClientConfig.*;

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
