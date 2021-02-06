package htwb.ai.controller.integration;

import htwb.ai.controller.GitVersionController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GitVersionControllerTest {

    @Test
    public void getVersionInfo() throws Exception {
        MockMvc gitVersionController = MockMvcBuilders.standaloneSetup(new GitVersionController()).build();
        ResultActions resultActions = gitVersionController.perform(MockMvcRequestBuilders.get("/version"))
                .andExpect(status().isOk());

        Assertions.assertTrue(resultActions.andReturn().getResponse().getContentAsString().length() > 0);
    }
}
