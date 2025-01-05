package pl.wrapper.parking.pwrResponseHandler.domain;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class PwrApiCaller {

    private final WebClient webClient;

    public Mono<List<ParkingResponse>> fetchParkingPlaces() {
        return webClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDummyMap())
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

    private static HashMap<String, String> createDummyMap() {
        HashMap<String, String> body = new HashMap<>();
        body.put("o", "get_parks");
        return body;
    }

    @SuppressWarnings("unchecked")
    private static List<ParkingResponse> parseResponse(Object unparsedResponse) throws ClassCastException {
        List<ParkingResponse> returnList = new ArrayList<>();
        ArrayList<Object> firstTierCastList = (ArrayList<Object>) unparsedResponse;
        int maxId = Integer.MIN_VALUE;
        for (Object currentParkingPrecast : firstTierCastList) {
            LinkedHashMap<String, String> currentParking;
            try {
                currentParking = (LinkedHashMap<String, String>) currentParkingPrecast;
            } catch (ClassCastException e) {
                continue;
            }
            int boundlessFreeSpots = Integer.parseInt(currentParking.getOrDefault("liczba_miejsc", "0"));
            int totalSpots = Integer.parseInt(currentParking.getOrDefault("places", "0"));
            int parkingId = Integer.parseInt(currentParking.getOrDefault("id", "0"));
            if (parkingId > maxId) maxId = parkingId;
            ParkingResponse currentResponse = ParkingResponse.builder()
                    .parkingId(parkingId)
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
                    .build();
            returnList.add(currentResponse);
        }
        makeParkingIdUnique(returnList, maxId + 1);
        return returnList;
    }

    private static void makeParkingIdUnique(List<ParkingResponse> returnList, int nextId) {
        for (int i = 0; i < returnList.size(); i++) {
            ParkingResponse response = returnList.get(i);
            if (response.parkingId() == 0) {
                ParkingResponse updated = ParkingResponse.builder()
                        .parkingId(nextId++)
                        .name(response.name())
                        .freeSpots(response.freeSpots())
                        .symbol(response.symbol())
                        .openingHours(response.openingHours())
                        .closingHours(response.closingHours())
                        .totalSpots(response.totalSpots())
                        .address(response.address())
                        .build();
                returnList.set(i, updated);
            }
        }
    }

    private static LocalTime getParsedTime(@Nullable String time) {
        if (time == null) return null;

        return LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME);
    }
}
