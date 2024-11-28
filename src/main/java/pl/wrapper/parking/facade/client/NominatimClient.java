package pl.wrapper.parking.facade.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import pl.wrapper.parking.facade.dto.NominatimLocation;

import java.util.List;

@HttpExchange("${maps.api.url}")
public interface NominatimClient {

    @GetExchange("/search")
    List<NominatimLocation> search(@RequestParam("q") String query,
                                   @RequestParam("format") String format);
}
