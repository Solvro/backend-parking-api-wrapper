package pl.wrapper.parking.facade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import pl.wrapper.parking.facade.client.NominatimClient;

@Configuration
public class NominatimClientConfig {

    @Bean
    public NominatimClient nominatimClient() {
        WebClientAdapter webClientAdapter = WebClientAdapter.create(WebClient.create());
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(webClientAdapter).build();

        return factory.createClient(NominatimClient.class);
    }
}
