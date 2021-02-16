package htwb.ai.controller.utils;

import htwb.ai.controller.model.SongData;
import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author : Enrico Gamil Toros
 * Project name : KBE-Beleg
 * @version : 1.0
 * @since : 15.02.21
 **/
@Service
public class RequestUtils {

    private RestTemplate restTemplate;

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Get all the songs made from the given artist.
     * This will make a request to song service.
     *
     * @param artist Artist to be searched
     * @param jwt Jwt token to be used in authorization header
     * @return Songs made from given artist, will throw exception if none found
     * @throws UnknownHostException     if host not resolved
     * @throws HttpClientErrorException if reply has no body
     * @throws NoSuchElementException   if no song for artist found
     */
    public List<SongData> getSongsFromArtist(String artist, String jwt) throws UnknownHostException, HttpClientErrorException, NoSuchElementException {
        restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<SongData[]> response = restTemplate.exchange(
                "http://" + InetAddress.getLocalHost().getHostAddress() + ":8080/songsWS/rest/songs?artist=" + artist,
                HttpMethod.GET, entity, SongData[].class);
        if (response.hasBody()) {
            return Arrays.asList(response.getBody());
        } else {
            throw new NoSuchElementException("No song found for artist: " + artist);
        }
    }

    /**
     * Get admin auth Token
     *
     * @return jwt Token
     * @throws HttpException            if Unauthorized or playlist service down
     * @throws UnknownHostException     if host not resolved
     * @throws HttpClientErrorException if reply has no body
     */
    private String getAuthToken() throws UnknownHostException, HttpClientErrorException, HttpException {
        String adminId = System.getenv("KBE_SUPER_USER_ID");
        String adminPassword = System.getenv("KBE_SUPER_USER_PASSWORD");
        String body = "{\"userId\":\"" + adminId + "\",\"password\":\"" + adminPassword + "\"}";

        ResponseEntity<String> response;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        response = restTemplate.exchange(
                "http://" + InetAddress.getLocalHost().getHostAddress() + ":8080/songsWS/rest/auth",
                HttpMethod.POST, entity, String.class);

        if (response.hasBody()) {
            return response.getBody();
        } else {
            throw new HttpException("Unauthorized");
        }
    }
}
