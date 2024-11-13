package pl.wrapper.parking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrapper.parking.utils.DataFetcher;

@RequestMapping("v1")
@RestController
@RequiredArgsConstructor
public class ParkingController {

    private final DataFetcher dataFetcher;

    @GetMapping("/fetch")
    public void fetchData() {
        System.out.println(dataFetcher.fetchParkingPlaces(1).subscribe(System.out::println));
    }
}
