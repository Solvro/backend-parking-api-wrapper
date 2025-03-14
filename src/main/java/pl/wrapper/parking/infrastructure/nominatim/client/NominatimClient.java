package pl.wrapper.parking.infrastructure.nominatim.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import pl.wrapper.parking.facade.dto.main.NominatimLocation;
import reactor.core.publisher.Flux;

@HttpExchange
public interface NominatimClient {

    @GetExchange("/search")
    Flux<NominatimLocation> search(
            @RequestParam("q") String query, @RequestParam(value = "format", defaultValue = "jsonv2") String format);
}
