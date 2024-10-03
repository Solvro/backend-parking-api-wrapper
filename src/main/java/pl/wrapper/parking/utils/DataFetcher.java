package pl.wrapper.parking.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
//TODO: przeniesc logike do Service
// Opracowac klase response dla callowania api PWR
// Podzielic to na mniejsze endpointy
// Zastanowic sie nad mechanizmem zapamietywania danych.

public class DataFetcher {


    private final WebClient webClient;

    public Mono<Map> fetchParkingPlaces() {
        return webClient.post()
                .uri("https://iparking.pwr.edu.pl/modules/iparking/scripts/ipk_operations.php")
                .header("Accept", "application/json")
                .header("Accept-Encoding", "gzip")
                .header("Accept-Language", "pl")
                .header("Referer", "https://iparking.pwr.edu.pl")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Connection", "keep-alive")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createBody())
                .retrieve()
                .bodyToMono(Map.class);
    }

    private Map<String, String> createBody() {
        Map<String, String> body = new HashMap<>();
        body.put("o", "get_parks");
        return body;
    }


}
