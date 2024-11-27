package pl.wrapper.parking.exception;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class ResultTest {
    @Autowired
    private MockMvc mockMvc;


    @Test
    void shouldReturnDummyBody() throws Exception {
        Long id = 4L;
        MvcResult mvcResult = mockMvc.perform(get("/id/{id}",id))//add url and variables
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();// get response body

        Integer status = mvcResult.getResponse().getStatus();//get response status

        Integer OkStatus = 200;
        assertEquals(status,OkStatus); //check status
        assertEquals(responseBody, String.valueOf(id));//check response body
    }
}

//WebMvcTest mockMvc - testRestTemplate -> how to check error body and how to check if outcome was 200-300