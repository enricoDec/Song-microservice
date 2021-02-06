package htwb.ai.MarvEn.controller.integration;

import htwb.ai.MarvEn.utils.JwtUtils;
import htwb.ai.MarvEn.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.mockito.Mockito.mock;

/**
 * @author : Enrico Gamil Toros
 * Project name : MarvEn
 * @version : 1.0
 * @since : 19.01.21
 **/
@ExtendWith(SystemStubsExtension.class)
public class JwtUtilsTest {
    private User user;
    @SystemStub
    private EnvironmentVariables environment = new EnvironmentVariables("SECRET_KEY_KBE", "test_secret_key");
    @BeforeEach
    void setUp() {
        user = new User("mmuser", "pass1234", "Test", "Tester");
    }

    @Test
    void createAndValidateJWT() {
        long creationTime = System.currentTimeMillis();
        String token = JwtUtils.createJWT(user.getUserId(), user.getLastName());
        Assertions.assertFalse(token.isBlank());
        Assertions.assertTrue(JwtUtils.verifyJWT(token));

        Claims claim = JwtUtils.decodeJWT(token);
        Assertions.assertEquals(claim.getId(), user.getUserId());
        Assertions.assertEquals(claim.getSubject(), user.getLastName());
        Assertions.assertTrue(claim.getExpiration().getTime() > creationTime);
    }
}
