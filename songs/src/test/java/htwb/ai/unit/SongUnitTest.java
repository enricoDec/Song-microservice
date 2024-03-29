package htwb.ai.unit;

import htwb.ai.controller.controller.SongController;
import htwb.ai.controller.model.Song;
import htwb.ai.controller.repo.SongRepository;
import htwb.ai.controller.utils.JwtDecode;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import javax.persistence.PersistenceException;
import java.util.Collections;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SystemStubsExtension.class)
class SongUnitTest {
    @SystemStub
    private final EnvironmentVariables environment = new EnvironmentVariables("SECRET_KEY_KBE", "test_secret_key");
    private MockMvc mockMvc;
    private SongRepository songRepository;
    private Song fullSong;
    private Song mockSong;

    @BeforeEach
    public void setup() {
        songRepository = mock(SongRepository.class);

        mockMvc = MockMvcBuilders.standaloneSetup(
                new SongController(songRepository)).build();

        fullSong = new Song("My humps", "Black Eyed Peas", "anyLabel", 2020);
        fullSong.setId(1);

        mockSong = mock(Song.class);
        when(songRepository.save(any())).thenReturn(mockSong);
    }

    @Test
    void getSongShouldReturnOKAndSongForExistingIdJSON() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            when(songRepository.findSongBySongId(1)).thenReturn(fullSong);
            mockMvc.perform(get("/songs/1")
                            .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    // verify JSON content
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("My humps"))
                    .andExpect(jsonPath("$.artist").value("Black Eyed Peas"))
                    .andExpect(jsonPath("$.label").value("anyLabel"))
                    .andExpect(jsonPath("$.released").value(2020));
        }
    }

    @Test
    void getSongShouldReturnOKAndSongForExistingIdXML() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            when(songRepository.findSongBySongId(1)).thenReturn(fullSong);
            mockMvc.perform(get("/songs/1")
                            .accept(MediaType.APPLICATION_XML_VALUE)
                            .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_XML_VALUE))
                    // verify xml content
                    .andExpect(xpath("Song/id").string("1"))
                    .andExpect(xpath("Song/title").string("My humps"))
                    .andExpect(xpath("Song/artist").string("Black Eyed Peas"))
                    .andExpect(xpath("Song/label").string("anyLabel"))
                    .andExpect(xpath("Song/released").string("2020"));
        }
    }

    @Test
    void getSongShouldReturnUnauthorized() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(false);

            mockMvc.perform(get("/songs/1")
                            .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void getSongBadIdNotExistingXML() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            when(songRepository.findSongBySongId(2)).thenReturn(null);
            mockMvc.perform(get("/songs/2")
                            .accept(MediaType.APPLICATION_XML_VALUE).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void getSongBadIdNotExistingJSON() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            when(songRepository.findSongBySongId(2)).thenReturn(null);
            mockMvc.perform(get("/songs/2")
                            .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void getSongBadInvalidIdJSON() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            when(songRepository.findSongBySongId(0)).thenReturn(null); // id is out of defined range
            mockMvc.perform(get("/songs/0")
                            .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void getSongBadInvalidIdXML() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            when(songRepository.findSongBySongId(0)).thenReturn(null);
            mockMvc.perform(get("/songs/0")
                            .accept(MediaType.APPLICATION_XML).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void getSongEdgeAcceptAllFormats() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            when(songRepository.findSongBySongId(1)).thenReturn(fullSong);
            mockMvc.perform(get("/songs/1")
                            .accept(MediaType.ALL).header(HttpHeaders.AUTHORIZATION, "BLOB")) // if accept header
                    // client is '*/*'
                    .andExpect(status().isOk());
        }
    }

    @Test
    void getSongBadAcceptHeader() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            when(songRepository.findSongBySongId(1)).thenReturn(fullSong);
            mockMvc.perform(get("/songs/1")
                            .accept(MediaType.TEXT_HTML).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNotAcceptable());
        }
    }

    @Test
    void getSongBadInvalidIdStringXML() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            mockMvc.perform(get("/songs/abc")
                            .accept(MediaType.APPLICATION_XML).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void getSongBadInvalidIdStringJSON() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            mockMvc.perform(get("/songs/abc")
                            .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void getAllSongsShouldReturnOKSongArrayJSON() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);


            when(songRepository.getAllSongs()).thenReturn(Collections.singletonList(fullSong));
            mockMvc.perform(get("/songs")
                            .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("My humps"))
                    .andExpect(jsonPath("$[0].artist").value("Black Eyed Peas"))
                    .andExpect(jsonPath("$[0].label").value("anyLabel"))
                    .andExpect(jsonPath("$[0].released").value(2020));
        }
    }

    @Test
    void getAllSongsShouldReturnOKSongArrayXML() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);
            when(mockSong.getId()).thenReturn(1);

            when(songRepository.getAllSongs()).thenReturn(Collections.singletonList(fullSong));
            mockMvc.perform(get("/songs")
                            .accept(MediaType.APPLICATION_XML).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/xml"))
                    .andExpect(xpath("List/item/id").string("1"))
                    .andExpect(xpath("List/item/title").string("My humps"))
                    .andExpect(xpath("List/item/artist").string("Black Eyed Peas"))
                    .andExpect(xpath("List/item/label").string("anyLabel"))
                    .andExpect(xpath("List/item/released").string("2020"));
        }
    }

    @Test
    void getAllSongsByArtist() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid("BLOB")).thenReturn(true);


            when(songRepository.findSongByArtist(fullSong.getArtist())).thenReturn(Collections.singletonList(fullSong));
            mockMvc.perform(get("/songs?artist=" + fullSong.getArtist())
                            .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("My humps"))
                    .andExpect(jsonPath("$[0].artist").value("Black Eyed Peas"))
                    .andExpect(jsonPath("$[0].label").value("anyLabel"))
                    .andExpect(jsonPath("$[0].released").value(2020));
        }
    }

    @Test
    void getAllSongsByArtistNotExisting() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid("BLOB")).thenReturn(true);


            when(songRepository.findSongByArtist("Enrico")).thenReturn(Collections.emptyList());
            mockMvc.perform(get("/songs?artist=Enrico")
                            .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void getAllSongsIsUnauthorized() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(false);

            mockMvc.perform(get("/songs")
                            .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void getAllSongsEdgeAcceptAll() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            when(songRepository.getAllSongs()).thenReturn(Collections.singletonList(fullSong));
            mockMvc.perform(get("/songs")
                            .accept(MediaType.ALL).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void getAllSongsEdgeEmptyArrayJSON() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid("BLOB")).thenReturn(true);

            when(songRepository.getAllSongs()).thenReturn(Collections.emptyList());
            mockMvc.perform(get("/songs")
                            .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void getAllSongsEdgeEmptyListXML() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid("BLOB")).thenReturn(true);

            when(songRepository.getAllSongs()).thenReturn(Collections.emptyList());
            mockMvc.perform(get("/songs")
                            .accept(MediaType.APPLICATION_XML).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void getAllSongsBadAcceptHeader() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            when(songRepository.getAllSongs()).thenReturn(Collections.emptyList());
            mockMvc.perform(get("/songs")
                            .accept(MediaType.TEXT_HTML).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNotAcceptable());
        }
    }

    @Test
    void addSongGood() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            ArgumentCaptor<Song> argument = ArgumentCaptor.forClass(Song.class);
            mockMvc.perform(post("/songs")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.APPLICATION_JSON).content("{\n" +
                                    "\t\t\"title\": \"Ghostbusters (I'm not a fraid)\",\n" +
                                    "\t\t\"artist\": \"Fall Out Boy, Missy Elliott\",\n" +
                                    "\t\t\"label\": \"Virgin\",\n" +
                                    "\t\t\"released\": 2016\n" +
                                    "\t}"))
                    .andExpect(status().isCreated());
            verify(songRepository).save(argument.capture());

            Assertions.assertEquals("Ghostbusters (I'm not a fraid)", argument.getValue().getTitle());
            Assertions.assertEquals("Fall Out Boy, Missy Elliott", argument.getValue().getArtist());
            Assertions.assertEquals("Virgin", argument.getValue().getLabel());
            Assertions.assertEquals(2016, argument.getValue().getReleased());
        }
    }

    @Test
    void addSongFailedToPersist() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid("BLOB")).thenReturn(true);

            when(songRepository.save(any())).thenThrow(PersistenceException.class);
            mockMvc.perform(post("/songs")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.APPLICATION_JSON).content("{\n" +
                                    "\t\t\"title\": \"Ghostbusters (I'm not a fraid)\",\n" +
                                    "\t\t\"artist\": \"Fall Out Boy, Missy Elliott\",\n" +
                                    "\t\t\"label\": \"Virgin\",\n" +
                                    "\t\t\"released\": 2016\n" +
                                    "\t}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void addSongGoodVerifyLocation() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);
            when(mockSong.getId()).thenReturn(1);

            mockMvc.perform(post("/songs")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.APPLICATION_JSON).content("{\n" +
                                    "\t\t\"title\": \"Ghostbusters (I'm not a fraid)\",\n" +
                                    "\t\t\"artist\": \"Fall Out Boy, Missy Elliott\",\n" +
                                    "\t\t\"label\": \"Virgin\",\n" +
                                    "\t\t\"released\": 2016\n" +
                                    "\t}"))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/rest/songs/1"));
        }
    }

    @Test
    void addSongIsUnauthorized() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(false);

            mockMvc.perform(post("/songs")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.APPLICATION_JSON).content("{\n" +
                                    "\t\t\"title\": \"Ghostbusters (I'm not a fraid)\",\n" +
                                    "\t\t\"artist\": \"Fall Out Boy, Missy Elliott\",\n" +
                                    "\t\t\"label\": \"Virgin\",\n" +
                                    "\t\t\"released\": 2016\n" +
                                    "\t}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void addSongGoodEdgeOnlyNecessaryValues() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);
            when(mockSong.getId()).thenReturn(1);

            ArgumentCaptor<Song> argument = ArgumentCaptor.forClass(Song.class);

            mockMvc.perform(post("/songs")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.APPLICATION_JSON).content("{\n" +
                                    "\t\t\"title\": \"Ghostbusters (I'm not a fraid)\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/rest/songs/1"));

            verify(songRepository).save(argument.capture());
            Assertions.assertEquals("Ghostbusters (I'm not a fraid)", argument.getValue().getTitle());
            Assertions.assertNull(argument.getValue().getArtist());
            Assertions.assertNull(argument.getValue().getLabel());
            Assertions.assertNull(argument.getValue().getReleased());
        }
    }

    @Test
    void addSongGoodEdgeJSONWithAdditionalKeys() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);
            when(mockSong.getId()).thenReturn(1);

            ArgumentCaptor<Song> argument = ArgumentCaptor.forClass(Song.class);

            mockMvc.perform(post("/songs")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.APPLICATION_JSON).content("{\n" +
                                    "\t\t\"title\": \"Ghostbusters (I'm not a fraid)\", \"key\": \"value\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/rest/songs/1"));

            verify(songRepository).save(argument.capture());
            Assertions.assertEquals("Ghostbusters (I'm not a fraid)", argument.getValue().getTitle());
            Assertions.assertNull(argument.getValue().getArtist());
            Assertions.assertNull(argument.getValue().getLabel());
            Assertions.assertNull(argument.getValue().getReleased());
        }
    }

    @Test
    void addSongBadJSONWithWrongKeys() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            mockMvc.perform(post("/songs")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.APPLICATION_JSON).content("{\n" +
                                    "\t\t\"key\": \"value\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void addSongBadJSONBadFormatted() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            mockMvc.perform(post("/songs")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.APPLICATION_JSON).content("{\n" +
                                    "\t\t\"title\": \"Ghostbusters (I'm not a fraid)\""))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void addSongBadUnsupportedMediaType() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            mockMvc.perform(post("/songs")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.TEXT_HTML).content("test message"))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Test
    void addSongWithId() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            mockMvc.perform(post("/songs")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.APPLICATION_JSON).content("{\n" +
                                    "    \"id\": 9999,\n" +
                                    "    \"title\": \"Can’t Stop the Feeling\",\n" +
                                    "    \"artist\": \"Justin Timberlake\", \n" +
                                    "    \"label\": \"Trolls\",\n" +
                                    "    \"released\": 2016\n" +
                                    "}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void postSongGoodJsonFile() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid("BLOB")).thenReturn(true);

            byte[] json = IOUtils.toByteArray(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(
                    "song.json")));

            MockMultipartFile file = new MockMultipartFile("file", "song.json",
                    MediaType.MULTIPART_FORM_DATA_VALUE, json);

            ArgumentCaptor<Song> argument = ArgumentCaptor.forClass(Song.class);
            mockMvc.perform(multipart("/songs")
                            .file(file)
                            .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isCreated());
            verify(songRepository).save(argument.capture());

            Assertions.assertEquals("Can’t Stop the Feeling", argument.getValue().getTitle());
            Assertions.assertEquals("Justin Timberlake", argument.getValue().getArtist());
            Assertions.assertEquals("Trolls", argument.getValue().getLabel());
            Assertions.assertEquals(2016, argument.getValue().getReleased());
        }
    }

    @Test
    void putSongGoodVerifyStatusCode() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);
            when(mockSong.getId()).thenReturn(1);
            when(songRepository.existsById(1)).thenReturn(true);
            when(songRepository.findSongBySongId(1)).thenReturn(mockSong);

            mockMvc.perform(
                            put("/songs/1")
                                    .header(HttpHeaders.AUTHORIZATION, "BLOB")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"id\": 1, \"title\": \"SONG_TITLE_EDIT\", \"artist\": \"COOL " +
                                            "Artitst_EDIT\", \"label\": \"SONY_EDIT\", \"released\": 2021}"))
                    .andExpect(status().isNoContent());
            verify(songRepository, atLeastOnce()).save(any());
        }
    }

    @Test
    void putSongDoesNotExist2() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            when(songRepository.existsById(2)).thenReturn(true);
            when(songRepository.findSongBySongId(2)).thenThrow(NullPointerException.class);
            mockMvc.perform(put("/songs/2")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"id\": 2, \"title\": \"SONG_TITLE_EDIT\", \"artist\": \"COOL Artitst_EDIT\", " +
                                    "\"label\": \"SONY_EDIT\", \"released\": 2021}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void putSongIsUnauthorized() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(false);

            mockMvc.perform(put("/songs/1")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"id\": 1, \"title\": \"SONG_TITLE_EDIT\", \"artist\": \"COOL Artitst_EDIT\", " +
                                    "\"label\": \"SONY_EDIT\", \"released\": 2021}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void putSongDoesNotExist() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);
            when(songRepository.existsById(2)).thenReturn(false);

            mockMvc.perform(put("/songs/2")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"id\": 2, \"title\": \"SONG_TITLE_EDIT\", \"artist\": \"COOL Artitst_EDIT\", " +
                                    "\"label\": \"SONY_EDIT\", \"released\": 2021}"))
                    .andExpect(status().isNotFound());
            verify(songRepository, times(0)).save(any());
        }
    }

    @Test
    void putDifferentIdInUrlAndPayload() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);
            when(mockSong.getId()).thenReturn(1);
            when(songRepository.existsById(1)).thenReturn(true);
            when(songRepository.findSongBySongId(1)).thenReturn(mockSong);

            mockMvc.perform(put("/songs/1")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"id\": 2, \"title\": \"SONG_TITLE_EDIT\", \"artist\": \"COOL Artitst_EDIT\", " +
                                    "\"label\": \"SONY_EDIT\", \"released\": 2021}"))
                    .andExpect(status().isBadRequest());
            verify(songRepository, times(0)).save(any());
        }
    }

    @Test
    void putEdgeCaseIdNotValid() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);
            when(mockSong.getId()).thenReturn(1);
            when(songRepository.existsById(1)).thenReturn(true);
            when(songRepository.findSongBySongId(1)).thenReturn(mockSong);

            mockMvc.perform(put("/songs/0")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"id\": 0, \"title\": \"SONG_TITLE_EDIT\", \"artist\": \"COOL Artitst_EDIT\", " +
                                    "\"label\": \"SONY_EDIT\", \"released\": 2021}"))
                    .andExpect(status().isNotFound());
            verify(songRepository, times(0)).save(any());
        }
    }

    @Test
    void putEmptyPayload() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            mockMvc.perform(put("/songs/0")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
            verify(songRepository, times(0)).save(any());
        }
    }

    @Test
    void deleteSongGoodVerifyStatusCode() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            mockMvc.perform(delete("/songs/1")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNoContent());
        }
    }

    @Test
    void deleteSongIsUnauthorized() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(false);

            mockMvc.perform(delete("/songs/1")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void deleteSongGoodSpyMethodCall() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            mockMvc.perform(delete("/songs/1")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNoContent());
            verify(songRepository, times(1)).deleteById(1);
        }
    }

    @Test
    void deleteSongBadIdOutOfRange() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);

            mockMvc.perform(delete("/songs/0")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void deleteSongBadStringInsteadOfId() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);
            mockMvc.perform(delete("/songs/abc")
                            .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void deleteSongBadSongNotExisting() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid(anyString())).thenReturn(true);
            when(mockSong.getId()).thenReturn(1);

            doThrow(EmptyResultDataAccessException.class).when(songRepository).deleteById(1);
            mockMvc.perform(
                            delete("/songs/1")
                                    .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNotFound());
        }
    }
}
