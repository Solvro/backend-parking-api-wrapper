package pl.wrapper.parking.pwrResponseHandler.domain;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.wrapper.parking.pwrResponseHandler.dto.Address;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;
import pl.wrapper.parking.pwrResponseHandler.exception.PwrApiNotRespondingException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
class PwrApiCaller {


    private final WebClient webClient;

    @SneakyThrows(JSONException.class)
    public Mono<List<ParkingResponse>> fetchParkingPlaces() {
        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> Mono.error(new PwrApiNotRespondingException(
                        LocalDateTime.now(),
                        clientResponse.statusCode()
                )))
                .bodyToMono(String.class)
                .map(PwrApiCaller::parseResponse);
    }

    private static List<ParkingResponse> parseResponse(String jsonResponse) throws JSONException {
        List<ParkingResponse> returnList = new ArrayList<>();
        JSONArray parkingJsonList = new JSONObject(jsonResponse).getJSONArray("places");
        for(int i = 0; i < parkingJsonList.length(); i++) {
            JSONObject currentParking = parkingJsonList.getJSONObject(i);
            ParkingResponse currentResponse = ParkingResponse.builder()
                    .parkingId(currentParking.getInt("id"))
                    .name(currentParking.getString("nazwa"))
                    .freeSpots(currentParking.getInt("liczba_miejsc"))
                    .symbol(currentParking.getString("symbol"))
                    .name(currentParking.getString("nazwa"))
                    .openingHours(LocalTime.parse(currentParking.getString("open_hours"), DateTimeFormatter.ofPattern("hh:mm:ss")))
                    .closingHours(LocalTime.parse(currentParking.getString("close_hour"), DateTimeFormatter.ofPattern("hh:mm:ss")))
                    .totalSpots(currentParking.getInt("places"))
                    .address(
                            new Address(
                                    currentParking.getString("address"),
                                    (float) currentParking.getDouble("geo_lat"),
                                    (float) currentParking.getDouble("geo_lan")
                            )
                    )
                    .build();
            returnList.add(currentResponse);
        }
        return returnList;
    }
}
