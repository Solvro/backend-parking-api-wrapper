package pl.wrapper.parking.pwrResponseHandler.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Profile("prod")
@Component
@RequiredArgsConstructor
public final class PwrApiCaller {

    private final WebClient webClient;

    public Mono<List<ParkingResponse>> fetchParkingPlaces() {
        return webClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDummyParkingMap())
                .retrieve()
                .bodyToMono(HashMap.class)
                .flatMap(parkingResponses -> {
                    try {
                        return Mono.just(parseResponse(parkingResponses.get("places")));
                    } catch (ClassCastException e) {
                        return Mono.error(e);
                    }
                });
    }

    private static HashMap<String, String> createDummyParkingMap() {
        HashMap<String, String> body = new HashMap<>();
        body.put("o", "get_parks");
        return body;
    }

    @SuppressWarnings("unchecked")
    private static List<ParkingResponse> parseResponse(Object unparsedResponse) throws ClassCastException {
        List<ParkingResponse> returnList = new ArrayList<>();
        ArrayList<Object> firstTierCastList = (ArrayList<Object>) unparsedResponse;
        int parkingId = 0;
        for (Object currentParkingPrecast : firstTierCastList) {
            LinkedHashMap<String, String> currentParking;
            try {
                currentParking = (LinkedHashMap<String, String>) currentParkingPrecast;
            } catch (ClassCastException e) {
                continue;
            }
            int boundlessFreeSpots = Integer.parseInt(currentParking.getOrDefault("liczba_miejsc", "0"));
            int totalSpots = Integer.parseInt(currentParking.getOrDefault("places", "0"));
            ParkingResponse currentResponse = ParkingResponse.builder()
                    .parkingId(++parkingId)
                    .name(currentParking.getOrDefault("nazwa", "unknown"))
                    .freeSpots(Math.max(0, Math.min(totalSpots, boundlessFreeSpots)))
                    .symbol(currentParking.getOrDefault("symbol", "unknown"))
                    .openingHours(PwrApiCaller.getParsedTime(currentParking.get("open_hour")))
                    .closingHours(PwrApiCaller.getParsedTime(currentParking.get("close_hour")))
                    .totalSpots(totalSpots)
                    .address(new Address(
                            currentParking.getOrDefault("address", "unknown").strip(),
                            Float.parseFloat(currentParking.get("geo_lat")),
                            Float.parseFloat(currentParking.get("geo_lan"))))
                    .trend(Short.parseShort(currentParking.getOrDefault("trend", "0")))
                    .build();
            returnList.add(currentResponse);
        }
        return returnList;
    }

    private static LocalTime getParsedTime(@Nullable String time) {
        if (time == null) return null;
        return LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME);
    }

    private static HashMap<String, String> createDummyChartMap(int forId) {
        HashMap<String, String> body = new HashMap<>();
        body.put("o", "get_today_chart");
        body.put("i", String.valueOf(forId));
        return body;
    }

    private static final Integer[] ID_MAPPER = {4, 2, 5, 6, 7};

    private Mono<Object> fetchParkingChart(int forId) {
        return webClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDummyChartMap(forId))
                .retrieve()
                .bodyToMono(HashMap.class)
                .flatMap(Mono::just);
    }

    public Mono<List<Object>> fetchAllParkingCharts() {
        return Flux.fromArray(ID_MAPPER)
                .flatMap(this::fetchParkingChart)
                .collectList();
    }
}
