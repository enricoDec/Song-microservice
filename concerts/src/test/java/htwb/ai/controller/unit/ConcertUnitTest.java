package htwb.ai.controller.unit;


import htwb.ai.controller.controller.ConcertsController;
import htwb.ai.controller.model.Concert;
import htwb.ai.controller.model.SongData;
import htwb.ai.controller.repo.ConcertsRepository;
import htwb.ai.controller.utils.JwtDecode;
import htwb.ai.controller.utils.RequestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import javax.persistence.PersistenceException;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author : Enrico Gamil Toros
 * Project name : MarvEn
 * @version : 1.0
 * @since : 19.01.21
 **/
@ExtendWith(SystemStubsExtension.class)
public class ConcertUnitTest {
    @SystemStub
    private final EnvironmentVariables environment = new EnvironmentVariables("SECRET_KEY_KBE", "test_secret_key");
    private ConcertsRepository concertsRepositoryMock;
    private MockMvc concertMvc;
    private RequestUtils requestUtilsMock;

    private SongData song;

    private Concert concert;
    private Concert concertNoSongs;

    @BeforeEach
    void setup() {
        //Mock repository
        concertsRepositoryMock = mock(ConcertsRepository.class);

        //Mock request utils to mock song microservice request
        requestUtilsMock = mock(RequestUtils.class);

        concertMvc = MockMvcBuilders.standaloneSetup(
                new ConcertsController(concertsRepositoryMock, requestUtilsMock)).build();

        //Mock a Song
        song = new SongData(1, "My humps", "Black Eyed Peas", "anyLabel", 2020);

        //Mock a concert 1
        concert = new Concert("Rome", "Black Eyed Peas", 10);
        concert.setConcertId(1L);

        //Mock a concert 1
        concertNoSongs = new Concert("Berlin", "Unknown Artist", 1);
        concertNoSongs.setConcertId(2L);
    }

    // ---------------------
    // Get all concerts
    // ---------------------

    @Test
    @DisplayName("Good Test Get all Concerts as JSON")
    void getConcertsGoodTestJson() throws Exception {
        String expectedJson = "[{\"concertId\":1,\"location\":\"Rome\",\"artist\":\"Black Eyed Peas\"," +
                "\"maxTickets\":10,\"songList\":[{\"id\":1,\"title\":\"My humps\",\"artist\":\"Black Eyed Peas\"," +
                "\"label\":\"anyLabel\",\"released\":2020}]},{\"concertId\":2,\"location\":\"Berlin\"," +
                "\"artist\":\"Unknown Artist\",\"maxTickets\":1,\"songList\":[]}]";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            //Mock Concert Repo to return two mock concerts
            when(concertsRepositoryMock.getAllConcerts()).thenReturn(Arrays.asList(concert, concertNoSongs));
            //Mock Song Endpoint request
            when(requestUtilsMock.getSongsFromArtist(concert.getArtist(), jwt)).thenReturn(Collections.singletonList(song));
            when(requestUtilsMock.getSongsFromArtist(concertNoSongs.getArtist(), jwt)).thenReturn(Collections.emptyList());
            ResultActions res = concertMvc.perform(MockMvcRequestBuilders
                            .get("/concerts/")
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isOk());

            Assertions.assertEquals(expectedJson, res.andReturn().getResponse().getContentAsString());
        }
    }

    @Test
    @DisplayName("Good Test Get all Concerts as Xml")
    void getConcertsGoodTestXml() throws Exception {
        String expectedXml = "<List><item><concertId>1</concertId><location>Rome</location><artist>Black Eyed " +
                "Peas</artist><maxTickets>10</maxTickets><songList><songList><id>1</id><title>My " +
                "humps</title><artist>Black Eyed Peas</artist><label>anyLabel</label><released>2020</released" +
                "></songList></songList></item><item><concertId>2</concertId><location>Berlin</location><artist" +
                ">Unknown Artist</artist><maxTickets>1</maxTickets><songList/></item></List>";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            //Mock Concert Repo to return two mock concerts
            when(concertsRepositoryMock.getAllConcerts()).thenReturn(Arrays.asList(concert, concertNoSongs));
            //Mock Song Endpoint request
            when(requestUtilsMock.getSongsFromArtist(concert.getArtist(), jwt)).thenReturn(Collections.singletonList(song));
            when(requestUtilsMock.getSongsFromArtist(concertNoSongs.getArtist(), jwt)).thenReturn(Collections.emptyList());
            ResultActions res = concertMvc.perform(MockMvcRequestBuilders
                            .get("/concerts/")
                            .accept(MediaType.APPLICATION_XML)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isOk());

            Assertions.assertEquals(expectedXml, res.andReturn().getResponse().getContentAsString());
        }
    }

    @Test
    @DisplayName("Bad Test invalid jwt")
    void getConcertsInvalidJwt() throws Exception {
        String jwt = "INVALID_TOKEN";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(false);

            concertMvc.perform(MockMvcRequestBuilders
                            .get("/concerts/")
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("No Concerts")
    void getConcertsNoConcerts() throws Exception {
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            //Mock Concert Repo to return two mock concerts
            when(concertsRepositoryMock.getAllConcerts()).thenReturn(Collections.emptyList());
            concertMvc.perform(MockMvcRequestBuilders
                            .get("/concerts/")
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isNotFound());

        }
    }

    @Test
    @DisplayName("No Songs for concert")
    void getConcertsNoSongForConcert() throws Exception {
        String expectedJson = "[{\"concertId\":1,\"location\":\"Rome\",\"artist\":\"Black Eyed Peas\"," +
                "\"maxTickets\":10,\"songList\":[]},{\"concertId\":2,\"location\":\"Berlin\",\"artist\":\"Unknown " +
                "Artist\",\"maxTickets\":1,\"songList\":[]}]";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            //Mock Concert Repo to return two mock concerts
            when(concertsRepositoryMock.getAllConcerts()).thenReturn(Arrays.asList(concert, concertNoSongs));
            //Mock Song Endpoint request
            when(requestUtilsMock.getSongsFromArtist(concert.getArtist(), jwt)).thenThrow(NoSuchElementException.class);
            ResultActions res = concertMvc.perform(MockMvcRequestBuilders
                            .get("/concerts/")
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isOk());

            Assertions.assertEquals(expectedJson, res.andReturn().getResponse().getContentAsString());
        }
    }

    // ---------------------
    // Get concert by id
    // ---------------------

    @Test
    @DisplayName("Good Test Get concert by id as JSON")
    void getConcertGoodTestJson() throws Exception {
        String expectedJson = "{\"concertId\":1,\"location\":\"Rome\",\"artist\":\"Black Eyed Peas\"," +
                "\"maxTickets\":10,\"songList\":[{\"id\":1,\"title\":\"My humps\",\"artist\":\"Black Eyed Peas\"," +
                "\"label\":\"anyLabel\",\"released\":2020}]}";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            //Mock Concert Repo to return two mock concerts
            when(concertsRepositoryMock.findById(concert.getConcertId())).thenReturn(java.util.Optional.ofNullable(concert));
            //Mock Song Endpoint request
            when(requestUtilsMock.getSongsFromArtist(concert.getArtist(), jwt)).thenReturn(Collections.singletonList(song));
            ResultActions res = concertMvc.perform(MockMvcRequestBuilders
                            .get("/concerts/" + concert.getConcertId())
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isOk());

            Assertions.assertEquals(expectedJson, res.andReturn().getResponse().getContentAsString());
        }
    }

    @Test
    @DisplayName("Good Test Get concert by id as XML")
    void getConcertGoodTestXml() throws Exception {
        String expectedJson = "<Concert><concertId>1</concertId><location>Rome</location><artist>Black Eyed " +
                "Peas</artist><maxTickets>10</maxTickets><songList><songList><id>1</id><title>My " +
                "humps</title><artist>Black Eyed Peas</artist><label>anyLabel</label><released>2020</released" +
                "></songList></songList></Concert>";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            //Mock Concert Repo to return two mock concerts
            when(concertsRepositoryMock.findById(concert.getConcertId())).thenReturn(java.util.Optional.ofNullable(concert));
            //Mock Song Endpoint request
            when(requestUtilsMock.getSongsFromArtist(concert.getArtist(), jwt)).thenReturn(Collections.singletonList(song));
            ResultActions res = concertMvc.perform(MockMvcRequestBuilders
                            .get("/concerts/" + concert.getConcertId())
                            .accept(MediaType.APPLICATION_XML)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isOk());

            Assertions.assertEquals(expectedJson, res.andReturn().getResponse().getContentAsString());
        }
    }

    @Test
    @DisplayName("Bad Test invalid jwt")
    void getConcertInvalidJwt() throws Exception {
        String jwt = "INVALID_TOKEN";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(false);

            concertMvc.perform(MockMvcRequestBuilders
                            .get("/concerts/" + concert.getConcertId())
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("Bad Test invalid concert Id")
    void getConcertInvalidId() throws Exception {
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            concertMvc.perform(MockMvcRequestBuilders
                            .get("/concerts/-1")
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("Get not existing concert by id")
    void getConcertIdNotFound() throws Exception {
        String jwt = "BLOB";
        Long notExistingConcert = 9999L;
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            //Mock Concert Repo to return two mock concerts
            when(concertsRepositoryMock.findById(notExistingConcert)).thenThrow(NoSuchElementException.class);
            concertMvc.perform(MockMvcRequestBuilders
                            .get("/concerts/" + notExistingConcert)
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("Get concert by id no song for concert")
    void getConcertNoSongForConcert() throws Exception {
        String expectedJson = "{\"concertId\":1,\"location\":\"Rome\",\"artist\":\"Black Eyed Peas\"," +
                "\"maxTickets\":10,\"songList\":[]}";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            //Mock Concert Repo to return two mock concerts
            when(concertsRepositoryMock.findById(concert.getConcertId())).thenReturn(java.util.Optional.ofNullable(concert));
            //Mock Song Endpoint request
            when(requestUtilsMock.getSongsFromArtist(concert.getArtist(), jwt)).thenThrow(NoSuchElementException.class);
            ResultActions res = concertMvc.perform(MockMvcRequestBuilders
                            .get("/concerts/" + concert.getConcertId())
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isOk());

            Assertions.assertEquals(expectedJson, res.andReturn().getResponse().getContentAsString());
        }
    }

    // ---------------------
    // Post a concert
    // ---------------------

    @Test
    @DisplayName("Post a new Concert as JSON")
    void postConcertGood() throws Exception {
        String jwt = "BLOB";
        String concertJson = "{\n" +
                "    \"location\": \"Rome\",\n" +
                "    \"artist\": \"Justin Timberlake\",\n" +
                "    \"maxTickets\": 100\n" +
                "}";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            when(concertsRepositoryMock.save(any())).thenReturn(concert);
            ResultActions res = concertMvc.perform(MockMvcRequestBuilders
                            .post("/concerts/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(concertJson)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isCreated());

            Assertions.assertEquals("/rest/concerts/" + concert.getConcertId(),
                    res.andReturn().getResponse().getHeader(HttpHeaders.LOCATION));
        }
    }

    @Test
    @DisplayName("Post a new Concert not authorized")
    void postConcertNotAuthorized() throws Exception {
        String jwt = "INVALID_TOKEN";
        String concertJson = "{\n" +
                "    \"location\": \"Rome\",\n" +
                "    \"artist\": \"Justin Timberlake\",\n" +
                "    \"maxTickets\": 100\n" +
                "}";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(false);

            when(concertsRepositoryMock.save(any())).thenReturn(concert);
            concertMvc.perform(MockMvcRequestBuilders
                            .post("/concerts/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(concertJson)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("Post a new Concert with location empty")
    void postConcertLocationEmpty() throws Exception {
        String jwt = "BLOB";
        String concertJson = "{\n" +
                "    \"location\": \"\",\n" +
                "    \"artist\": \"Justin Timberlake\",\n" +
                "    \"maxTickets\": 100\n" +
                "}";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            concertMvc.perform(MockMvcRequestBuilders
                            .post("/concerts/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(concertJson)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("Post a new Concert with artist empty")
    void postConcertArtistEmpty() throws Exception {
        String jwt = "BLOB";
        String concertJson = "{\n" +
                "    \"location\": \"Berlin\",\n" +
                "    \"artist\": \"\",\n" +
                "    \"maxTickets\": 100\n" +
                "}";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            concertMvc.perform(MockMvcRequestBuilders
                            .post("/concerts/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(concertJson)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("Post a new Concert with maxTickets empty")
    void postConcertMaxTicketsEmpty() throws Exception {
        String jwt = "BLOB";
        String concertJson = "{\n" +
                "    \"location\": \"Berlin\",\n" +
                "    \"artist\": \"Artic Monkeys\",\n" +
                "    \"maxTickets\": \n" +
                "}";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            concertMvc.perform(MockMvcRequestBuilders
                            .post("/concerts/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(concertJson)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("Post a new Concert with maxTickets negative")
    void postConcertMaxTicketsNegative() throws Exception {
        String jwt = "BLOB";
        String concertJson = "{\n" +
                "    \"location\": \"Berlin\",\n" +
                "    \"artist\": \"Artic Monkeys\",\n" +
                "    \"maxTickets\": -1\n" +
                "}";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            concertMvc.perform(MockMvcRequestBuilders
                            .post("/concerts/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(concertJson)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("Post a new Concert with empty json")
    void postConcertEmptyBody() throws Exception {
        String jwt = "BLOB";
        String concertJson = "{}";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            concertMvc.perform(MockMvcRequestBuilders
                            .post("/concerts/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(concertJson)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("Post new concert db error")
    void postConcertNoConcert() throws Exception {
        String jwt = "BLOB";
        String concertJson = "{\n" +
                "    \"location\": \"Rome\",\n" +
                "    \"artist\": \"Justin Timberlake\",\n" +
                "    \"maxTickets\": 100\n" +
                "}";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.isJwtValid(jwt)).thenReturn(true);

            when(concertsRepositoryMock.save(any())).thenThrow(PersistenceException.class);
            concertMvc.perform(MockMvcRequestBuilders
                            .post("/concerts/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(concertJson)
                            .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isBadRequest());
        }
    }
}
