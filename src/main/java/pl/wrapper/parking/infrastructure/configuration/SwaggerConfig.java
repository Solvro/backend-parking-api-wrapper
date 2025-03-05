package pl.wrapper.parking.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        Info info = new Info();
        info.title("Parking API");
        info.description("Wrapper for old PWr API for parking lots");
        return new OpenAPI()
                .servers(List.of(
                        new Server()
                                .url("https://parking-api.topwr.solvro.pl/parkingiAPI")
                                .description("HTTPS server version"),
                        new Server().url("http://localhost:8080/parkingiAPI").description("HTTP server version")))
                .info(info);
    }
}
