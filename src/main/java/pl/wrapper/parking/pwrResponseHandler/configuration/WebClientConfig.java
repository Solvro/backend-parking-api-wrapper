package pl.wrapper.parking.pwrResponseHandler.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class WebClientConfig {

    @Bean
    public WebClient webClient() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Accept", "application/json");
        headers.add("Accept-Encoding", "gzip");
        headers.add("Accept-Language", "pl");
        headers.add("Referer", "https://iparking.pwr.edu.pl");
        headers.add("X-Requested-With", "XMLHttpRequest");
        headers.add("Connection", "keep-alive");
        return WebClient.builder().baseUrl("https://iparking.pwr.edu.pl/modules/iparking/scripts/ipk_operations.php")
                .defaultHeaders(httpHeaders -> httpHeaders.addAll(headers))
                .build();
    }

}
