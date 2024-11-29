package pl.wrapper.parking.facade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import pl.wrapper.parking.facade.client.NominatimClient;
import pl.wrapper.parking.facade.exception.NominatimClientException;
import reactor.core.publisher.Mono;

@Configuration
public class NominatimClientConfig {

    @Value("${maps.api.url}")
    private String mapsUrl;

    @Bean
    public NominatimClient nominatimClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(mapsUrl)
                .defaultStatusHandler(HttpStatusCode::isError,
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new NominatimClientException(body))))
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient)).build();

        return factory.createClient(NominatimClient.class);
    }
}
