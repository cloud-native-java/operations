package demo.health;

import demo.ActuatorApplication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ActuatorApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class EmotionalRestControllerTest {

 private Log log = LogFactory.getLog(getClass());

 @Autowired
 private MockMvc mockMvc;

 @Autowired
 public void configureTimeToLive(HealthEndpoint endpoint) {
  endpoint.setTimeToLive(0);
 }

 @Test
 public void events() throws Exception {
  this.mockMvc.perform(get("/event/happy")).andExpect(
   MockMvcResultMatchers.status().isOk());
  this.confirmHealthEndpointStatus("UP", HappyEvent.class, status().isOk());
  this.mockMvc.perform(get("/event/sad")).andExpect(
   MockMvcResultMatchers.status().isOk());
  this.confirmHealthEndpointStatus("DOWN", SadEvent.class, status().is(503));
 }

 private void confirmHealthEndpointStatus(String status,
  Class<? extends EmotionalEvent> ec, ResultMatcher rm) throws Exception {
  this.mockMvc.perform(get("/health"))
   .andDo(mvcResult -> log.info(mvcResult.getResponse().getContentAsString()))
   .andExpect(jsonPath("$.emotional.status", containsString(status)))
   .andExpect(jsonPath("$.emotional.class", containsString(ec.getName())))
   .andExpect(rm);
 }

}