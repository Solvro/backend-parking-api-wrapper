package pl.wrapper.parking.pwrResponseHandler.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public final class PwrApiCaller {


    private final WebClient webClient;

    public Mono<List<ParkingResponse>> fetchParkingPlaces() {
        return webClient.post()
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
        for(Object currentParkingPrecast : firstTierCastList) {
            LinkedHashMap<String, String> currentParking;
            try{
                 currentParking = (LinkedHashMap<String, String>) currentParkingPrecast;
            }catch(ClassCastException e){
                continue;
            }
            ParkingResponse currentResponse = ParkingResponse.builder()
                    .parkingId(Integer.parseInt(currentParking.getOrDefault("id", "0")))
                    .name(currentParking.getOrDefault("nazwa", "unknown"))
                    .freeSpots(Integer.parseInt(currentParking.getOrDefault("liczba_miejsc", "0")))
                    .symbol(currentParking.getOrDefault("symbol", "unknown"))
                    .openingHours(PwrApiCaller.getParsedTime(currentParking.get("open_hour")))
                    .closingHours(PwrApiCaller.getParsedTime(currentParking.get("close_hour")))
                    .totalSpots(Integer.parseInt(currentParking.getOrDefault("places", "0")))
                    .address(
                            new Address(
                                    currentParking.getOrDefault("address", "unknown"),
                                    Float.parseFloat(currentParking.get("geo_lat")),
                                    Float.parseFloat(currentParking.get("geo_lan"))
                            )
                    )
                    .build();
            returnList.add(currentResponse);
        }
        return returnList;
    }

    private static LocalTime getParsedTime(@Nullable String time){
        if(time == null) return null;

        return LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME);
    }
}
