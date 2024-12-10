package pl.wrapper.parking.pwrResponseHandler;

import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

import java.util.List;

public interface PwrApiServerCaller {
    List<ParkingResponse> fetchData();
}
