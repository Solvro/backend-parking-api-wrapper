package pl.wrapper.parking.pwrResponseHandler.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import pl.wrapper.parking.infrastructure.exception.PwrApiNotRespondingException;
import reactor.core.publisher.Mono;

@Configuration
class WebClientConfig {

    @Profile("prod")
    @Bean
    public WebClient webClient() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Accept", "application/json");
        headers.add("Accept-Encoding", "gzip");
        headers.add("Accept-Language", "pl");
        headers.add("Referer", "https://iparking.pwr.edu.pl");
        headers.add("X-Requested-With", "XMLHttpRequest");
        headers.add("Connection", "keep-alive");
        return WebClient.builder()
                .baseUrl("https://iparking.pwr.edu.pl/modules/iparking/scripts/ipk_operations.php")
                .defaultHeaders(httpHeaders -> httpHeaders.addAll(headers))
                .filter(buildRetryFilter())
                .filter(ExchangeFilterFunction.ofResponseProcessor(WebClientConfig::responseFilter))
                .build();
    }

    static Mono<ClientResponse> responseFilter(ClientResponse response) {
        if (response.statusCode().isError())
            return response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new PwrApiNotRespondingException(body)));
        return Mono.just(response);
    }

    static ExchangeFilterFunction buildRetryFilter() {
        return (request, next) -> next.exchange(request)
                .onErrorResume(PwrApiNotRespondingException.class, ex -> next.exchange(request)
                )
                .retry(2);
    }
}
