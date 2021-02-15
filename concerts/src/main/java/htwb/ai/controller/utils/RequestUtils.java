package htwb.ai.controller.utils;

import org.apache.http.HttpException;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author : Enrico Gamil Toros
 * Project name : KBE-Beleg
 * @version : 1.0
 * @since : 15.02.21
 **/
public class RequestUtils {

    /**
     * Get admin auth Token
     *
     * @return jwt Token
     * @throws HttpException if Unauthorized or playlist service down
     */
    private String getAuthToken() throws HttpException {
        String adminId = System.getenv("KBE_SUPER_USER_ID");
        String adminPassword = System.getenv("KBE_SUPER_USER_PASSWORD");
        String body = "{\"userId\":\"" + adminId + "\",\"password\":\"" + adminPassword + "\"}";

        ResponseEntity<String> response = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            response = restTemplate.exchange(
                    "http://" + InetAddress.getLocalHost().getHostAddress() + ":8080/songsWS/rest/auth",
                    HttpMethod.POST, entity, String.class);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (response != null)
            return response.getBody();
        else
            throw new HttpException("Unauthorized");
    }
}
