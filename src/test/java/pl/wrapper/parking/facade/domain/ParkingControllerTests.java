package pl.wrapper.parking.facade.domain;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ParkingControllerTests {

    @LocalServerPort
    private String port;

    @Value("${server.servlet.context-path}")
    private String apiUrlPath;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void TestGetByParamsWithNoParams() throws JSONException {
        String url = "http://localhost:" + port + apiUrlPath;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);

        JSONArray jsonArray = new JSONArray(response.getBody());

        assertDoesNotThrow(() -> JSONException.class
                ,jsonArray.getJSONObject(0).getString("parkingId"));

    }

    @Test
    public void TestGetByParamsWithParams() throws JSONException {
        String url = "http://localhost:" + port + apiUrlPath + "/?symbol=WRO";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);

        JSONArray jsonArray = new JSONArray(response.getBody());

        assertDoesNotThrow(() -> JSONException.class
                ,jsonArray.getJSONObject(0).getString("parkingId"));
    }

    @Test
    public void TestGetByParamsNoParkingFound() throws JSONException {
        String url = "http://localhost:" + port + apiUrlPath + "/?symbol=TESTNOTFOUND";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);

        JSONArray jsonArray = new JSONArray(response.getBody());

        assertThrows(JSONException.class
                ,() -> jsonArray.getJSONObject(0));
    }
}
