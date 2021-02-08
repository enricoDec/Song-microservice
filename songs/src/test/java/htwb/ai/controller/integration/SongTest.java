//package htwb.ai.controller.integration;
//
//import htwb.ai.controller.AuthController;
//import htwb.ai.controller.SongController;
//import htwb.ai.model.Song;
//import htwb.ai.repo.SongRepository;
//import htwb.ai.repo.UserRepository;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.ResultActions;
//import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
//import uk.org.webcompere.systemstubs.jupiter.SystemStub;
//import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
//
//import java.util.NoSuchElementException;
//import java.util.Objects;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;
//
///**
// * @author : Enrico Gamil Toros
// * Project name : MarvEn
// * @version : 1.0
// * @since : 19.01.21
// **/
//
//@SpringBootTest
//@TestPropertySource(locations = "/test.properties")
//@ExtendWith(SystemStubsExtension.class)
//public class SongTest {
//    private MockMvc mockMvc;
//    private Song defaultSong;
//    private String mmusterToken;
//
//    @SystemStub
//    private EnvironmentVariables environment = new EnvironmentVariables("SECRET_KEY_KBE", "test_secret_key");
//
//    @Autowired
//    private SongRepository songRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @BeforeEach
//    public void setup() throws Exception {
//        songRepository.deleteAll();
//        defaultSong = songRepository.save(new Song("My humps", "Black Eyed Peas", "anyLabel", 2020));
//
//        String mmusterAuth = "{\"userId\": \"mmuster\" , \"password\": \"pass1234\"}";
//
//        mockMvc = MockMvcBuilders.standaloneSetup(new SongController(songRepository)).build();
//        MockMvc authMockMvc = MockMvcBuilders.standaloneSetup(new AuthController(userRepository)).build();
//
//        mmusterToken = authMockMvc.perform(
//                post("/auth")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mmusterAuth))
//                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
//    }
//
//
//    @Test
//    public void getSongGood() throws Exception {
//        ResultActions res = mockMvc.perform(MockMvcRequestBuilders
//                .get("/songs/" + defaultSong.getId())
//                .accept(MediaType.APPLICATION_JSON)
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken)).
//                andExpect(status().isOk());
//
//        // verify json content
//        Assertions.assertEquals(res.andReturn().getResponse().getContentAsString(),
//                "{\"id\":" + defaultSong.getId() + ",\"title\":\"My humps\",\"artist\":\"Black Eyed Peas\",\"label\":\"anyLabel\",\"released\":2020}");
//    }
//
//    @Test
//    public void getSongGoodXML() throws Exception {
//        ResultActions res = mockMvc.perform(MockMvcRequestBuilders
//                .get("/songs/" + defaultSong.getId())
//                .accept(MediaType.APPLICATION_XML)
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isOk());
//
//        // verify xml content
//        Assertions.assertEquals("" +
//                        "<Song><id>" + defaultSong.getId() + "</id><title>My humps</title>" +
//                        "<artist>Black Eyed Peas</artist>" +
//                        "<label>anyLabel</label>" +
//                        "<released>2020</released>" +
//                        "</Song>",
//                res.andReturn().getResponse().getContentAsString());
//    }
//
//    @Test
//    void getSongBadIdNotExistingXML() throws Exception {
//        mockMvc.perform(get("/songs/200")
//                .accept(MediaType.APPLICATION_XML_VALUE)
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void getSongBadIdNotExistingJSON() throws Exception {
//        mockMvc.perform(get("/songs/200")
//                .accept(MediaType.APPLICATION_JSON)
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void getSongBadInvalidIdJSON() throws Exception {
//        mockMvc.perform(get("/songs/0")
//                .accept(MediaType.APPLICATION_JSON)
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void getSongBadInvalidIdXML() throws Exception {
//        mockMvc.perform(get("/songs/0")
//                .accept(MediaType.APPLICATION_XML)
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void getSongEdgeAcceptAllFormats() throws Exception {
//        mockMvc.perform(get("/songs/" + defaultSong.getId())
//                .accept(MediaType.ALL)
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isOk());
//
//    }
//
//    @Test
//    void getSongBadAcceptHeader() throws Exception {
//        mockMvc.perform(get("/songs/" + defaultSong.getId())
//                .accept(MediaType.TEXT_HTML)
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isNotAcceptable());
//    }
//
//    @Test
//    void getSongBadInvalidIdStringXML() throws Exception {
//        mockMvc.perform(get("/songs/abc")
//                .accept(MediaType.APPLICATION_XML).header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void getSongBadInvalidIdStringJSON() throws Exception {
//        mockMvc.perform(get("/songs/abc")
//                .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void getAllSongsShouldReturnOKSongArrayJSON() throws Exception {
//        Song testSong1 = songRepository.save(new Song("Titel", "Artist", "Label", 2020));
//        Song testSong2 = songRepository.save(new Song("Titel2", "Artist2", "Label2", 2021));
//
//        ResultActions res = mockMvc.perform(get("/songs")
//                .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isOk());
//
//        Assertions.assertEquals(
//                "[{\"id\":" + defaultSong.getId() + ",\"title\":\"My humps\",\"artist\":\"Black Eyed Peas\",\"label\":\"anyLabel\",\"released\":2020}," +
//                        "{\"id\":" + testSong1.getId() + ",\"title\":\"Titel\",\"artist\":\"Artist\",\"label\":\"Label\",\"released\":2020}," +
//                        "{\"id\":" + testSong2.getId() + ",\"title\":\"Titel2\",\"artist\":\"Artist2\",\"label\":\"Label2\",\"released\":2021}]"
//                , res.andReturn().getResponse().getContentAsString());
//    }
//
//    @Test
//    void getAllSongsShouldReturnOKSongArrayXML() throws Exception {
//        Song testSong1 = songRepository.save(new Song("Titel", "Artist", "Label", 2020));
//        Song testSong2 = songRepository.save(new Song("Titel2", "Artist2", "Label2", 2021));
//
//        ResultActions res = mockMvc.perform(get("/songs")
//                .accept(MediaType.APPLICATION_XML).header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType("application/xml"));
//
//
//        Assertions.assertEquals(
//                "<List>" +
//                        "<item><id>" + defaultSong.getId() + "</id><title>My humps</title><artist>Black Eyed Peas</artist><label>anyLabel</label><released>2020</released></item>" +
//                        "<item><id>" + testSong1.getId() + "</id><title>Titel</title><artist>Artist</artist><label>Label</label><released>2020</released></item>" +
//                        "<item><id>" + testSong2.getId() + "</id><title>Titel2</title><artist>Artist2</artist><label>Label2</label><released>2021</released></item></List>"
//                , res.andReturn().getResponse().getContentAsString());
//    }
//
//    @Test
//    void getAllSongsEdgeAcceptAll() throws Exception {
//        mockMvc.perform(get("/songs")
//                .accept(MediaType.ALL).header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void getAllSongsEdgeEmptyArrayJSON() throws Exception {
//        songRepository.deleteAll();
//
//        mockMvc.perform(get("/songs")
//                .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$").isEmpty());
//    }
//
//    @Test
//    void getAllSongsEdgeEmptyListXML() throws Exception {
//        songRepository.deleteAll();
//
//        mockMvc.perform(get("/songs")
//                .accept(MediaType.APPLICATION_XML).header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_XML))
//                .andExpect(xpath("List/item").nodeCount(0));
//    }
//
//    @Test
//    void getAllSongsBadAcceptHeader() throws Exception {
//        mockMvc.perform(get("/songs")
//                .accept(MediaType.TEXT_HTML).header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isNotAcceptable());
//    }
//
//    @Test
//    void addSongGood() throws Exception {
//        ResultActions res = mockMvc.perform(
//                post("/songs")
//                        .header(HttpHeaders.AUTHORIZATION, mmusterToken)
//                        .contentType(MediaType.APPLICATION_JSON).content("{\n" +
//                        "\t\t\"title\": \"Ghostbusters (I'm not a fraid)\",\n" +
//                        "\t\t\"artist\": \"Fall Out Boy, Missy Elliott\",\n" +
//                        "\t\t\"label\": \"Virgin\",\n" +
//                        "\t\t\"released\": 2020\n" +
//                        "\t}"))
//                .andExpect(status().isCreated());
//
//        //get id of new song from location header
//        int location = Integer.parseInt(Objects.requireNonNull(res.andReturn().getResponse().getHeader(HttpHeaders.LOCATION)).replace("/rest/songs/", ""));
//        Assertions.assertEquals("Ghostbusters (I'm not a fraid)", songRepository.findSongBySongId(location).getTitle());
//        Assertions.assertEquals("Fall Out Boy, Missy Elliott", songRepository.findSongBySongId(location).getArtist());
//        Assertions.assertEquals("Virgin", songRepository.findSongBySongId(location).getLabel());
//        Assertions.assertEquals(2020, songRepository.findSongBySongId(location).getReleased());
//    }
//
//    @Test
//    void addSongGoodFromFile() throws Exception {
//        MockMultipartFile jsonFile = new MockMultipartFile("file", "",
//                "application/json", ("{\"title\": \"Mom\",\"artist\": \"Meghan Trainor, Kelli " +
//                "Trainor\",\"label\": \"Thank You\",\"released\": 2016}").getBytes());
//        MockHttpServletRequestBuilder builder =
//                MockMvcRequestBuilders.multipart("/songs")
//                        .file(jsonFile).header(HttpHeaders.AUTHORIZATION, mmusterToken);
//        mockMvc.perform(builder).andExpect(status().isCreated());
//    }
//
//    @Test
//    void addSongBadFromFileTitleMissing() throws Exception {
//        MockMultipartFile jsonFile = new MockMultipartFile("file", "",
//                "application/json", ("{\"artist\": \"Meghan Trainor, Kelli " +
//                "Trainor\",\"label\": \"Thank You\",\"released\": 2016}").getBytes());
//        MockHttpServletRequestBuilder builder =
//                MockMvcRequestBuilders.multipart("/songs")
//                        .file(jsonFile).header(HttpHeaders.AUTHORIZATION, mmusterToken);
//        mockMvc.perform(builder).andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void addSongBadFromFileEmptyFile() throws Exception {
//        MockMultipartFile jsonFile = new MockMultipartFile("file", "",
//                "application/json", ("").getBytes());
//        MockHttpServletRequestBuilder builder =
//                MockMvcRequestBuilders.multipart("/songs")
//                        .file(jsonFile).header(HttpHeaders.AUTHORIZATION, mmusterToken);
//        mockMvc.perform(builder).andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void addSongBadFromFileTitleUnauthorized() throws Exception {
//        MockMultipartFile jsonFile = new MockMultipartFile("file", "",
//                "application/json", ("{\"artist\": \"Meghan Trainor, Kelli " +
//                "Trainor\",\"label\": \"Thank You\",\"released\": 2016}").getBytes());
//        MockHttpServletRequestBuilder builder =
//                MockMvcRequestBuilders.multipart("/songs")
//                        .file(jsonFile).header(HttpHeaders.AUTHORIZATION, "wrong_token");
//        mockMvc.perform(builder).andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    void addSongGoodVerifyLocation() throws Exception {
//        mockMvc.perform(post("/songs")
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken)
//                .contentType(MediaType.APPLICATION_JSON).content("{\n" +
//                        "\t\t\"title\": \"Ghostbusters (I'm not a fraid)\",\n" +
//                        "\t\t\"artist\": \"Fall Out Boy, Missy Elliott\",\n" +
//                        "\t\t\"label\": \"Virgin\",\n" +
//                        "\t\t\"released\": 2020\n" +
//                        "\t}"))
//                .andExpect(status().isCreated())
//                .andReturn().getResponse().getHeader(HttpHeaders.LOCATION).contains("/rest/songs/");
//    }
//
//    @Test
//    void addSongGoodEdgeOnlyNecessaryValues() throws Exception {
//        ResultActions res = mockMvc.perform(
//                post("/songs")
//                        .header(HttpHeaders.AUTHORIZATION, mmusterToken)
//                        .contentType(MediaType.APPLICATION_JSON).content("{\n" +
//                        "\t\t\"title\": \"Ghostbusters (I'm not a fraid)\"}"))
//                .andExpect(status().isCreated());
//
//        //get id of new song from location header
//        int location = Integer.parseInt(Objects.requireNonNull(res.andReturn().getResponse().getHeader(HttpHeaders.LOCATION)).replace("/rest/songs/", ""));
//        Assertions.assertEquals("Ghostbusters (I'm not a fraid)", songRepository.findSongBySongId(location).getTitle());
//        Assertions.assertNull(songRepository.findSongBySongId(location).getArtist());
//        Assertions.assertNull(songRepository.findSongBySongId(location).getLabel());
//        Assertions.assertNull(songRepository.findSongBySongId(location).getReleased());
//    }
//
//    @Test
//    void addSongGoodEdgeJSONWithAdditionalKeys() throws Exception {
//        ResultActions res = mockMvc.perform(post("/songs")
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken)
//                .contentType(MediaType.APPLICATION_JSON).content("{\n" +
//                        "\t\t\"title\": \"Ghostbusters (I'm not a fraid)\", \"key\": \"value\"}"))
//                .andExpect(status().isCreated());
//
//        //get id of new song from location header
//        int location = Integer.parseInt(Objects.requireNonNull(res.andReturn().getResponse().getHeader(HttpHeaders.LOCATION)).replace("/rest/songs/", ""));
//        Assertions.assertEquals("Ghostbusters (I'm not a fraid)", songRepository.findSongBySongId(location).getTitle());
//        Assertions.assertNull(songRepository.findSongBySongId(location).getArtist());
//        Assertions.assertNull(songRepository.findSongBySongId(location).getLabel());
//        Assertions.assertNull(songRepository.findSongBySongId(location).getReleased());
//    }
//
//    @Test
//    void addSongBadJSONWithWrongKeys() throws Exception {
//        mockMvc.perform(post("/songs")
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken)
//                .contentType(MediaType.APPLICATION_JSON).content("{\n" +
//                        "\t\t\"key\": \"value\"}"))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void addSongBadJSONBadFormatted() throws Exception {
//        mockMvc.perform(post("/songs")
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken)
//                .contentType(MediaType.APPLICATION_JSON).content("{\n" +
//                        "\t\t\"title\": \"Ghostbusters (I'm not a fraid)\""))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void addSongBadUnsupportedMediaType() throws Exception {
//        mockMvc.perform(post("/songs")
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken)
//                .contentType(MediaType.TEXT_HTML).content("test message"))
//                .andExpect(status().isUnsupportedMediaType());
//    }
//
//    @Test
//    void deleteSongGoodVerifyStatusCode() throws Exception {
//        mockMvc.perform(delete("/songs/" + defaultSong.getId())
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isNoContent());
//
//        Assertions.assertNull(songRepository.findSongBySongId(defaultSong.getId()));
//    }
//
//    @Test
//    void deleteSongGood() throws Exception {
//        mockMvc.perform(delete("/songs/" + defaultSong.getId())
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken));
//
//        Assertions.assertNull(songRepository.findSongBySongId(defaultSong.getId()));
//    }
//
//    @Test
//    void deleteSongBadIdOutOfRange() throws Exception {
//        mockMvc.perform(delete("/songs/0")
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void deleteSongBadStringInsteadOfId() throws Exception {
//        mockMvc.perform(delete("/songs/abc")
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void deleteSongBadSongNotExisting() throws Exception {
//        songRepository.deleteAll();
//
//        mockMvc.perform(delete("/songs/" + defaultSong.getId())
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void putSongGoodVerifyStatusCode() throws Exception {
//        mockMvc.perform(put("/songs/" + defaultSong.getId())
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("{\"id\": " + defaultSong.getId() + ", \"title\": \"SONG_TITLE_EDIT\", \"artist\": \"COOL Artitst_EDIT\", \"label\": \"SONY_EDIT\", \"released\": 2021}"))
//                .andExpect(status().isNoContent());
//
//        Song editedSong = songRepository.findById(defaultSong.getId()).get();
//
//        Assertions.assertEquals(editedSong.getTitle(), "SONG_TITLE_EDIT");
//        Assertions.assertEquals(editedSong.getArtist(), "COOL Artitst_EDIT");
//        Assertions.assertEquals(editedSong.getLabel(), "SONY_EDIT");
//        Assertions.assertEquals(editedSong.getReleased(), 2021);
//
//    }
//
//    @Test
//    void putSongDoesNotExist() throws Exception {
//        mockMvc.perform(put("/songs/200")
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("{\"id\": 200, \"title\": \"SONG_TITLE_EDIT\", \"artist\": \"COOL Artitst_EDIT\", \"label\": \"SONY_EDIT\", \"released\": 2021}"))
//                .andExpect(status().isNotFound());
//
//        Assertions.assertThrows(NoSuchElementException.class, () -> songRepository.findById(200).get());
//    }
//
//    @Test
//    void putDifferentIdInUrlAndPayload() throws Exception {
//        mockMvc.perform(put("/songs/1")
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("{\"id\": 2, \"title\": \"SONG_TITLE_EDIT\", \"artist\": \"COOL Artitst_EDIT\", \"label\": \"SONY_EDIT\", \"released\": 2021}"))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void putEdgeCaseIdNotValid() throws Exception {
//        mockMvc.perform(put("/songs/0")
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("{\"id\": 0, \"title\": \"SONG_TITLE_EDIT\", \"artist\": \"COOL Artitst_EDIT\", \"label\": \"SONY_EDIT\", \"released\": 2021}"))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void putEmptyPayload() throws Exception {
//        mockMvc.perform(put("/songs/0")
//                .header(HttpHeaders.AUTHORIZATION, mmusterToken)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(""))
//                .andExpect(status().isBadRequest());
//    }
//}
