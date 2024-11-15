package pl.wrapper.parking.facade.domain;

import org.springframework.stereotype.Service;
import pl.wrapper.parking.facade.ParkingService;
import pl.wrapper.parking.pwrResponseHandler.PwrApiServerCaller;

@Service
record ParkingServiceImpl(PwrApiServerCaller pwrApiServerCaller) implements ParkingService {

}
