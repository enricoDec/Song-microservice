package htwb.ai.controller.integration;


import htwb.ai.controller.controller.AuthController;
import htwb.ai.controller.repo.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ExtendWith(SystemStubsExtension.class)
class AuthTest {
    private MockMvc mockMvc;
    @SystemStub
    private final EnvironmentVariables environment = new EnvironmentVariables("SECRET_KEY_KBE", "test_secret_key");
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setupMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(userRepository)).build();
    }

    @Test
    public void authUserGoodVerifyTokenFormat() throws Exception {
        String authorization = "{\"userId\":\"mmuster\",\"password\":\"pass1234\"}";
        ResultActions res = mockMvc.perform(MockMvcRequestBuilders
                .post("/auth").contentType(MediaType.APPLICATION_JSON)
                .content(authorization));
        res.andExpect(status().isOk());
        Assertions.assertTrue(res.andReturn().getResponse().getContentAsString().length() > 0);
    }

    @Test
    public void authUserBadUnauthorized() throws Exception {
        String authorization = "{\"userId\": \"test\", \"password\": \"ygfgyyg\"}";
        mockMvc.perform(MockMvcRequestBuilders
                .post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authorization))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void emptyBody() throws Exception {
        String authorization = "{}";
        mockMvc.perform(MockMvcRequestBuilders.post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authorization))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void oneKeyEmpty() throws Exception {
        String authorization = "{\"userId\": \"\", \"password\": \"ygfgyyg\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authorization))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void bothKeyEmpty() throws Exception {
        String authorization = "{\"userId\": \"\", \"password\": \"\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authorization))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void additionalKey() throws Exception {
        String authorization = "{\"userId\": \"\", \"password\": \"\", \"blob\": \"blub\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authorization))
                .andExpect(status().isBadRequest());
    }
}