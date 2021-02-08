package htwb.ai.controller.unit;

import htwb.ai.utils.JwtUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;


/**
 * @author : Enrico Gamil Toros
 * Project name : MarvEn
 * @version : 1.0
 * @since : 18.01.21
 **/
@ExtendWith(SystemStubsExtension.class)
public class JwtUtilsUnitTest {
    @SystemStub
    private EnvironmentVariables environment = new EnvironmentVariables("SECRET_KEY_KBE", "test_secret_key");
    @Test
    void createJWTGood() {
        Assertions.assertTrue(JwtUtils.createJWT("Test", "MrTest").length() > 0);
    }

    @Test
    void createJWTsameParams() {
        Assertions.assertNotEquals(JwtUtils.createJWT("Test", "MtTest1"), JwtUtils.createJWT("Test", "MtTest2"));
    }

    @Test
    void createJWTsameParams2() {
        Assertions.assertNotEquals(JwtUtils.createJWT("Test1", "MtTest"), JwtUtils.createJWT("Test2", "MtTest"));
    }

    @Test
    void createJWTIdEmpty() {
        try {
            JwtUtils.createJWT("", "MtTest");
            Assertions.fail("Id can't be blank");
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    @Test
    void createJWTSubjectEmpty() {
        try {
            JwtUtils.createJWT("Id", "");
            Assertions.fail("Subject can't be blank");
        } catch (IllegalArgumentException e) {
            //ok
        }
    }
}
