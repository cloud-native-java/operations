package demo.health;

import demo.Application;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebAppConfiguration
public class EmotionalRestControllerTest {

    @Autowired
    public void configureTimeToLive(HealthEndpoint endpoint) {
        endpoint.setTimeToLive(0);
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    private Log log = LogFactory.getLog(getClass());

    private MockMvc mockMvc;

    @Before
    public void begin() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    public void events() throws Exception {
        this.mockMvc.perform(get("/event/happy")).andExpect(MockMvcResultMatchers.status().isOk());
        this.confirmHealthEndpointStatus("UP", HappyEvent.class, status().isOk());
        this.mockMvc.perform(get("/event/sad")).andExpect(MockMvcResultMatchers.status().isOk());
        this.confirmHealthEndpointStatus("DOWN", SadEvent.class, status().is(503));
    }

    private void confirmHealthEndpointStatus(String status, Class<? extends EmotionalEvent> ec, ResultMatcher rm) throws Exception {
        this.mockMvc.perform(get("/health"))
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.emotional.status", containsString(status)))
                .andExpect(jsonPath("$.emotional.class", containsString(ec.getName())))
                .andExpect(rm);
    }

}