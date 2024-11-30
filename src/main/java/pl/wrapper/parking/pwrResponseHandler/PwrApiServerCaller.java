package pl.wrapper.parking.pwrResponseHandler;

import java.util.List;
import pl.wrapper.parking.pwrResponseHandler.dto.ParkingResponse;

public interface PwrApiServerCaller {
    List<ParkingResponse> fetchData();
}
