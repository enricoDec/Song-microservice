package htwb.ai.unit;

import htwb.ai.controller.utils.JwtDecode;
import io.jsonwebtoken.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;


/**
 * @author : Enrico Gamil Toros
 * Project name : MarvEn
 * @version : 1.0
 * @since : 18.01.21
 **/
@ExtendWith(SystemStubsExtension.class)
public class JwtUtilsUnitTest {
    private static final Key key = new SecretKeySpec(System.getenv("SECRET_KEY_KBE").getBytes(),
            SignatureAlgorithm.HS256.getJcaName());
    @SystemStub
    private final EnvironmentVariables environment = new EnvironmentVariables("SECRET_KEY_KBE", "test_secret_key");

    private static String createJWT(String id, String subject, int expiration) throws IllegalArgumentException {
        if (id.isBlank() || subject.isBlank()) {
            throw new IllegalArgumentException("Id and subject parameters can't be blank!");
        }
        JwtBuilder builder = Jwts.builder()
                .setId(id)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, System.getenv("SECRET_KEY_KBE").getBytes());
        return builder.compact();
    }

    @Test
    @DisplayName("Good Test")
    void decodeJwtGood() {
        String testToken = createJWT("mmuster", "Test", 90000000);
        Claims claim = JwtDecode.decodeJWT(testToken);
        Assertions.assertTrue(claim.getSubject().equals("Test") && claim.getId().equals("mmuster"));
    }

    @Test
    @DisplayName("Expired JWT")
    void decodeJwtExpired() {
        String testToken = createJWT("mmuster", "Test", 0);
        Assertions.assertThrows(ExpiredJwtException.class, () -> JwtDecode.decodeJWT(testToken));
    }

    @Test
    @DisplayName("Malformed JWT")
    void decodeJwtMalformed() {
        Assertions.assertThrows(MalformedJwtException.class, () -> JwtDecode.decodeJWT("BLOB"));
    }

    @Test
    @DisplayName("Good Test")
    void isValidJwtGoodTest() {
        String testToken = createJWT("mmuster", "Test", 999999);
        Assertions.assertTrue(JwtDecode.isJwtValid(testToken));
    }

    @Test
    @DisplayName("Bad Test")
    void isValidJwtBadTest() {
        String testToken = createJWT("mmuster", "Test", 0);
        Assertions.assertFalse(JwtDecode.isJwtValid(testToken));
    }
}
