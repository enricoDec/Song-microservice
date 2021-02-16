package htwb.ai.controller.unit;

import htwb.ai.controller.model.SongData;
import htwb.ai.controller.utils.RequestUtils;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
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
        requestUtils = new RequestUtils();
        restTemplateMock = mock(RestTemplate.class);
        responseEntityMock = mock(ResponseEntity.class);
        songData = new SongData[]{new SongData(1, "Song Title", "Artist", "Label", 2020)};
    }

    @Test
    @DisplayName("Request Utils Good Test")
    void requestUtilsGoodTest() throws UnknownHostException {
        List<SongData> songDataList;

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "BLOB");
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        requestUtils.setRestTemplate(restTemplateMock);
        when(restTemplateMock.exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(),
                ArgumentMatchers.<Class<SongData[]>>any()))
                .thenReturn(responseEntityMock);

        when(responseEntityMock.hasBody()).thenReturn(true);
        when(responseEntityMock.getBody()).thenReturn(songData);
        try {
            songDataList = requestUtils.getSongsFromArtist("Enrico", "BLOB");
        } catch (UnknownHostException | HttpClientErrorException | NoSuchElementException e) {
            Assertions.fail();
        }
    }
}
