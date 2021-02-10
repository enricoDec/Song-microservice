package htwb.ai.controller;

import htwb.ai.controller.controller.AuthController;
import htwb.ai.controller.model.User;
import htwb.ai.controller.repo.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import javax.persistence.NoResultException;

import java.util.Objects;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author : Enrico Gamil Toros
 * Project name : MarvEn
 * @version : 1.0
 * @since : 18.01.21
 **/
@ExtendWith(SystemStubsExtension.class)
public class AuthControllerUnitTest {
    @SystemStub
    private EnvironmentVariables environment = new EnvironmentVariables("SECRET_KEY_KBE", "test_secret_key");
    private MockMvc mockMvc;
    private User validUser;
    private UserRepository userRepository;


    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AuthController(userRepository)).build();
        validUser = new User("mmuster", "pass1234", "Maxime", "Muster");
    }

    /**
     * Good Case 1
     */
    @Test
    void goodCaseTest() throws Exception {
        when(userRepository.findUserByUserId(validUser.getUserId())).thenReturn(validUser);
        mockMvc.perform(post("/auth").contentType(MediaType.APPLICATION_JSON).content(
                "{\"userId\": \"mmuster\" , \"password\": \"pass1234\"}"))
                .andExpect(status().isOk());

        // to validate credentials the method 'getUserByUserId' in UserDAO should be called
        verify(userRepository, Mockito.atLeastOnce()).findUserByUserId(validUser.getUserId());
    }

    /**
     * Test Token
     */
    @Test
    void tokenTest() throws Exception {
        when(userRepository.findUserByUserId(validUser.getUserId())).thenReturn(validUser);
        ResultActions result = mockMvc
                .perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"mmuster\" , \"password\": \"pass1234\"}"))
                .andExpect(status().isOk());
        Assertions.assertTrue(result.andReturn().getResponse().getContentAsString().length() > 10);
        Assertions.assertTrue(Objects.requireNonNull(result.andReturn().getResponse().getContentType()).contains(MediaType.TEXT_PLAIN_VALUE));
    }

    /**
     * Unauthorized User
     */
    @Test
    void unauthorizedUserTest() throws Exception {
        when(userRepository.findUserByUserId(validUser.getUserId())).thenThrow(NoResultException.class);
        mockMvc.perform(post("/auth").contentType(MediaType.APPLICATION_JSON).content(
                "{\"userId\": \"mmuster\" , \"password\": \"pass1234\"}"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Wrong Password
     */
    @Test
    void wrongPasswordTest() throws Exception {
        when(userRepository.findUserByUserId(validUser.getUserId())).thenReturn(validUser);
        mockMvc.perform(post("/auth").contentType(MediaType.APPLICATION_JSON).content(
                "{\"userId\": \"mmuster\" , \"password\": \"wrongPassword\"}"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Edge additional keys in json
     */
    @Test
    void additionalKeysInJsonTest() throws Exception {
        when(userRepository.findUserByUserId(validUser.getUserId())).thenReturn(validUser);
        mockMvc.perform(post("/auth").contentType(MediaType.APPLICATION_JSON).content(
                "{\"userId\": \"mmuster\" , \"password\": \"pass1234\", \"totallyLegitKey\": \"blob\"}"))
                .andExpect(status().isOk());
    }

    /**
     * wrong JSON
     */
    @Test
    void wrongJSONTest() throws Exception {
        when(userRepository.findUserByUserId(validUser.getUserId())).thenReturn(validUser);
        mockMvc.perform(post("/auth").contentType(MediaType.APPLICATION_JSON).content(
                "{\"title\": \"SONG_TITLE\", \"artist\": \"COOL Artitst\", \"label\": \"SONY\", \"released\": 2020}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * No password
     */
    @Test
    void noPasswordTest() throws Exception {
        when(userRepository.findUserByUserId(validUser.getUserId())).thenReturn(validUser);
        mockMvc.perform(post("/auth").contentType(MediaType.APPLICATION_JSON).content(
                "{\"userId\": \"mmuster\"}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * No user
     */
    @Test
    void noUserTest() throws Exception {
        when(userRepository.findUserByUserId(validUser.getUserId())).thenReturn(validUser);
        mockMvc.perform(post("/auth").contentType(MediaType.APPLICATION_JSON).content(
                "{\"password\": \"pass1234\"}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Wrong User
     */
    @Test
    void wrongUserTest() throws Exception {
        when(userRepository.findUserByUserId(anyString())).thenThrow(NoResultException.class);
        mockMvc.perform(post("/auth").contentType(MediaType.APPLICATION_JSON).content(
                "{\"userId\": \"IM_A_USER\" , \"password\": \"pass1234\"}"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Wrong User
     */
    @Test
    void userFoundButNull() throws Exception {
        when(userRepository.findUserByUserId(anyString())).thenReturn(null);
        mockMvc.perform(post("/auth").contentType(MediaType.APPLICATION_JSON).content(
                "{\"userId\": \"IM_A_USER\" , \"password\": \"pass1234\"}"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Parameter Test
     */
    @Test
    void paramTest() throws Exception {
        when(userRepository.findUserByUserId(validUser.getUserId())).thenReturn(validUser);
        mockMvc.perform(post("/authssss").contentType(MediaType.APPLICATION_JSON).content(
                "{\"userId\": \"mmuster\" , \"password\": \"pass1234\"}"))
                .andExpect(status().isNotFound());
    }

    /**
     * Empty Json
     */
    @Test
    void emptyJsonTest() throws Exception {
        when(userRepository.findUserByUserId(validUser.getUserId())).thenReturn(validUser);
        mockMvc.perform(post("/auth").contentType(MediaType.APPLICATION_JSON).content(
                "{}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Not supported Content Type
     */
    @Test
    void notSupportedContentType() throws Exception {
        when(userRepository.findUserByUserId(validUser.getUserId())).thenReturn(validUser);
        mockMvc.perform(post("/auth").contentType(MediaType.APPLICATION_XML).content(
                "<root>\n" +
                        "   <password>pass1234</password>\n" +
                        "   <userId>mmuster</userId>\n" +
                        "</root>\n"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // Yes really
    @Test
    void trivialMethodsForCoverage(){
        validUser.setFirstName(validUser.getFirstName());
        validUser.setLastName("Test");
    }
}
