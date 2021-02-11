package htwb.ai.controller.unit;

import htwb.ai.controller.utils.JwtCompile;
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
public class JwtCompileUnitTest {
    @SystemStub
    private final EnvironmentVariables environment = new EnvironmentVariables("SECRET_KEY_KBE", "test_secret_key");
    @Test
    void createJWTGood() {
        Assertions.assertTrue(JwtCompile.createJWT("Test", "MrTest").length() > 0);
    }

    @Test
    void createJWTsameParams() {
        Assertions.assertNotEquals(JwtCompile.createJWT("Test", "MtTest1"), JwtCompile.createJWT("Test", "MtTest2"));
    }

    @Test
    void createJWTsameParams2() {
        Assertions.assertNotEquals(JwtCompile.createJWT("Test1", "MtTest"), JwtCompile.createJWT("Test2", "MtTest"));
    }

    @Test
    void createJWTIdEmpty() {
        try {
            JwtCompile.createJWT("", "MtTest");
            Assertions.fail("Id can't be blank");
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    @Test
    void createJWTSubjectEmpty() {
        try {
            JwtCompile.createJWT("Id", "");
            Assertions.fail("Subject can't be blank");
        } catch (IllegalArgumentException e) {
            //ok
        }
    }
}
