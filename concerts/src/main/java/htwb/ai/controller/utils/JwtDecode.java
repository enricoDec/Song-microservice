package htwb.ai.controller.utils;

import io.jsonwebtoken.*;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;


public class JwtDecode {
    private static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private static final byte[] secret_key = System.getenv("SECRET_KEY_KBE").getBytes();
    private static final Key key = new SecretKeySpec(secret_key, signatureAlgorithm.getJcaName());
    private static final long EXPIRATION_TIME = 90000000; // 25 hours

    /**
     * Check if JWT is valid
     * If JWT valid returns true, else false
     * @param jwt JWT to check
     * @return true if JWT valid, else false
     */
    public static boolean isJwtValid(String jwt) {
        try {
            decodeJWT(jwt);
        } catch (ExpiredJwtException | UnsupportedJwtException |
                MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /**
     * Check if JWT is valid, returns Claims if valid else throws Exception
     *
     * @param jwt JWT to validate
     * @return Claims
     * @throws ExpiredJwtException      ExpiredJwtException
     * @throws UnsupportedJwtException  UnsupportedJwtException
     * @throws MalformedJwtException    MalformedJwtException
     * @throws SignatureException       SignatureException
     * @throws IllegalArgumentException IllegalArgumentException
     */
    public static Claims decodeJWT(String jwt) throws ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, SignatureException, IllegalArgumentException {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(jwt).getBody();
    }
}
