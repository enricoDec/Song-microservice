package htwb.ai.controller.unit;

import htwb.ai.controller.model.SongData;
import htwb.ai.controller.utils.RequestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;

/**
 * @author : Enrico Gamil Toros
 * Project name : KBE-Beleg
 * @version : 1.0
 * @since : 16.02.21
 **/
public class RequestUtilsTest {
    private RestTemplate restTemplateMock;
    private ResponseEntity<SongData[]> responseEntityMock;
    private RequestUtils requestUtils;
    private SongData[] songData;

    @BeforeEach
    void setup() {
        restTemplateMock = mock(RestTemplate.class);
        responseEntityMock = mock(ResponseEntity.class);
        requestUtils = new RequestUtils(restTemplateMock);
        songData = new SongData[]{new SongData(1, "Song Title", "Artist", "Label", 2020)};
    }

    @Test
    @DisplayName("Request Utils Good Test")
    void requestUtilsGoodTest() throws UnknownHostException {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "BLOB");
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        try {
            requestUtils.getSongsFromArtist("Enrico", "BLOB");
            verify(restTemplateMock, atLeastOnce()).exchange("http://" + InetAddress.getLocalHost().getHostAddress() + ":8080/songsWS/rest/songs?artist=Enrico",
                    HttpMethod.GET, entity, SongData[].class);
            Assertions.fail();
        } catch (HttpClientErrorException | ResourceAccessException e) {
            //do nothing
        }
    }

    @Test
    @DisplayName("Request Utils Bad Test")
    void requestUtilsBadTest() throws UnknownHostException {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "BLOB");
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        try {
            requestUtils.getSongsFromArtist("Enrico", "BLOB");
            when(restTemplateMock.exchange("http://" + InetAddress.getLocalHost().getHostAddress() + ":8080/songsWS/rest/songs?artist=Enrico",
                    HttpMethod.GET, entity, SongData[].class)).thenReturn(responseEntityMock);
            when(responseEntityMock.hasBody()).thenReturn(false);
            Assertions.fail();
        } catch (HttpClientErrorException | ResourceAccessException e) {
            //do nothing
        }
    }

    @Test
    @DisplayName("Request Utils Bad Test No Song found")
    void requestUtilsBadNoSongsTest() throws UnknownHostException {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "BLOB");
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        try {
            requestUtils.getSongsFromArtist("Enrico", "BLOB");
            when(restTemplateMock.exchange("http://" + InetAddress.getLocalHost().getHostAddress() + ":8080/songsWS/rest/songs?artist=Enrico",
                    HttpMethod.GET, entity, SongData[].class)).thenReturn(responseEntityMock);
            when(responseEntityMock.hasBody()).thenReturn(false);
            when(responseEntityMock.getBody()).thenThrow(NoSuchElementException.class);
            Assertions.fail();
        } catch (HttpClientErrorException | ResourceAccessException e) {
            //do nothing
        }
    }
}
