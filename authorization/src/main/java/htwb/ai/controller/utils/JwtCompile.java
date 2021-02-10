package htwb.ai.controller.utils;

import io.jsonwebtoken.*;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;


public class JwtCompile {
    private static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private static final byte[] secret_key = System.getenv("SECRET_KEY_KBE").getBytes();
    private static final Key key = new SecretKeySpec(secret_key, signatureAlgorithm.getJcaName());
    private static final long EXPIRATION_TIME = 90000000; // 25 hours

    /**
     * Create a JWT
     *
     * @param id      unique id to be encoded in JWT
     * @param subject jwt subject
     * @return JWT
     * @throws IllegalArgumentException if id or subject are empty
     */
    public static String createJWT(String id, String subject) throws IllegalArgumentException {
        if (id.isBlank() || subject.isBlank()) {
            throw new IllegalArgumentException("Id and subject parameters can't be blank!");
        }
        JwtBuilder builder = Jwts.builder()
                .setId(id)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(signatureAlgorithm, secret_key);
        return builder.compact();
    }
}
