package htwb.ai.unit;

import htwb.ai.controller.controller.PlaylistController;
import htwb.ai.controller.model.Playlist;
import htwb.ai.controller.model.Song;
import htwb.ai.controller.repo.PlaylistRepository;
import htwb.ai.controller.repo.SongRepository;
import htwb.ai.controller.utils.JwtDecode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author : Enrico Gamil Toros
 * Project name : MarvEn
 * @version : 1.0
 * @since : 19.01.21
 **/
@ExtendWith(SystemStubsExtension.class)
public class PlaylistUnitTest {
    @SystemStub
    private final EnvironmentVariables environment = new EnvironmentVariables("SECRET_KEY_KBE", "test_secret_key");
    private MockMvc playlistMvc;
    private SongRepository songRepository;
    private PlaylistRepository playlistRepository;
    private Song defaultSong1;
    private Song defaultSong2;
    private Song mockSong;
    private Song mockSong2;
    private Playlist defaultPrivatePlaylist;
    private Playlist mockPlaylist;
    private Playlist defaultPublicPlaylist;
    private Playlist mockPlaylistPrivate;

    @BeforeEach
    void setup() {
        songRepository = mock(SongRepository.class);
        playlistRepository = mock(PlaylistRepository.class);

        playlistMvc = MockMvcBuilders.standaloneSetup(
                new PlaylistController(playlistRepository, songRepository)).build();

        defaultSong1 = new Song("My humps", "Black Eyed Peas", "anyLabel", 2020);
        defaultSong1.setId(1);
        mockSong = mock(Song.class);
        when(songRepository.save(any())).thenReturn(mockSong);

        defaultSong2 = new Song("We Built This City", "Starship", "Grunt/RCA", 1985);
        defaultSong2.setId(2);
        mockSong2 = mock(Song.class);
        when(songRepository.save(any())).thenReturn(mockSong2);

        defaultPublicPlaylist = new Playlist("Playlist", false, "mmuster");
        defaultPublicPlaylist.setId(1);
        defaultPublicPlaylist.addSong(defaultSong1);
        defaultPublicPlaylist.addSong(defaultSong2);
        mockPlaylist = mock(Playlist.class);
        when(playlistRepository.save(any())).thenReturn(mockPlaylist);

        defaultPrivatePlaylist = new Playlist("Playlist", true, "mmuster");
        defaultPrivatePlaylist.setId(2);
        defaultPrivatePlaylist.addSong(defaultSong1);
        defaultPrivatePlaylist.addSong(defaultSong2);
        mockPlaylistPrivate = mock(Playlist.class);
        when(playlistRepository.save(any())).thenReturn(mockPlaylistPrivate);
    }

    // ---------------------
    // Get playlist by Id
    // ---------------------

    @Test
    void getPlaylistByIdGoodTest() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT("BLOB")).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            when(playlistRepository.getPlaylistById(defaultPublicPlaylist.getId())).thenReturn(defaultPublicPlaylist);
            ResultActions res = playlistMvc.perform(MockMvcRequestBuilders
                    .get("/songLists/" + defaultPublicPlaylist.getId())
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isOk());

            Assertions.assertEquals(
                    "{\"id\":" + defaultPublicPlaylist.getId() + ",\"name\":\"Playlist\",\"isPrivate\":false,\"ownerId\":\"mmuster\",\"songList\":" +
                            "[{\"id\":" + defaultSong1.getId() + ",\"title\":\"My humps\",\"artist\":\"Black Eyed Peas\",\"label\":\"anyLabel\",\"released\":2020}," +
                            "{\"id\":" + defaultSong2.getId() + ",\"title\":\"We Built This City\",\"artist\":\"Starship\",\"label\":\"Grunt/RCA\",\"released\":1985}]}",
                    res.andReturn().getResponse().getContentAsString());
        }
    }

    @Test
    void getPlaylistByIdPlaylistDoesNotExistBadTest() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            int notExisitingPlaylistId = 9999;
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT("BLOB")).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            when(playlistRepository.getPlaylistById(notExisitingPlaylistId)).thenReturn(null);
            playlistMvc.perform(MockMvcRequestBuilders
                    .get("/songLists/" + notExisitingPlaylistId)
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void getPlaylistByIdBadId() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT("BLOB")).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            when(playlistRepository.getPlaylistById(0)).thenReturn(null);
            playlistMvc.perform(MockMvcRequestBuilders
                    .get("/songLists/" + "0")
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void getPrivatePlaylistByIdNotOwnerBadTest() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT("BLOB")).thenReturn(claim);
            when(claim.getId()).thenReturn("echuler");

            when(playlistRepository.getPlaylistById(defaultPrivatePlaylist.getId())).thenReturn(defaultPrivatePlaylist);
            playlistMvc.perform(MockMvcRequestBuilders
                    .get("/songLists/" + defaultPrivatePlaylist.getId())
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void getPlaylistBadJWTBadTest() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.decodeJWT("BLOB")).thenThrow(IllegalArgumentException.class);

            when(playlistRepository.getPlaylistById(defaultPrivatePlaylist.getId())).thenReturn(defaultPrivatePlaylist);
            playlistMvc.perform(MockMvcRequestBuilders
                    .get("/songLists/" + defaultPrivatePlaylist.getId())
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ---------------------
    // GET playlist by owner
    // ---------------------

    @Test
    void getPlaylistByOwnerGoodTest() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid("BLOB")).thenReturn(true);
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT(anyString())).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            when(playlistRepository.getAllByOwnerId("mmuster")).thenReturn(Arrays.asList(defaultPublicPlaylist, defaultPrivatePlaylist));
            ResultActions res = playlistMvc.perform(MockMvcRequestBuilders
                    .get("/songLists?userId=mmuster")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isOk());

            Assertions.assertEquals(
                    "[{\"id\":" + defaultPublicPlaylist.getId() + ",\"name\":\"Playlist\",\"isPrivate\":false,\"ownerId\":\"mmuster\",\"songList\":" +
                            "[{\"id\":" + defaultSong1.getId() + ",\"title\":\"My humps\",\"artist\":\"Black Eyed Peas\",\"label\":\"anyLabel\",\"released\":2020}," +
                            "{\"id\":" + defaultSong2.getId() + ",\"title\":\"We Built This City\",\"artist\":\"Starship\",\"label\":\"Grunt/RCA\",\"released\":1985}]}," +

                            "{\"id\":" + defaultPrivatePlaylist.getId() + ",\"name\":\"Playlist\",\"isPrivate\":true,\"ownerId\":\"mmuster\",\"songList\":" +
                            "[{\"id\":" + defaultSong1.getId() + ",\"title\":\"My humps\",\"artist\":\"Black Eyed Peas\",\"label\":\"anyLabel\",\"released\":2020}," +
                            "{\"id\":" + defaultSong2.getId() + ",\"title\":\"We Built This City\",\"artist\":\"Starship\",\"label\":\"Grunt/RCA\",\"released\":1985}]}]",
                    res.andReturn().getResponse().getContentAsString());
        }
    }

    @Test
    void getPlaylistByOwnerEmptyPlaylist() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid("BLOB")).thenReturn(true);
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT(anyString())).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            when(playlistRepository.getAllByOwnerId("mmuster")).thenReturn(Collections.emptyList());
            playlistMvc.perform(MockMvcRequestBuilders
                    .get("/songLists?userId=mmuster")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void getPlaylistByNotOwnerGoodTest() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid("BLOB")).thenReturn(true);
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT(anyString())).thenReturn(claim);
            when(claim.getId()).thenReturn("eschuler");

            when(playlistRepository.getAllPublicByOwnerId("mmuster")).thenReturn(Collections.singletonList(defaultPublicPlaylist));
            ResultActions res = playlistMvc.perform(MockMvcRequestBuilders
                    .get("/songLists?userId=mmuster")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isOk());

            Assertions.assertEquals(
                    "[{\"id\":" + defaultPublicPlaylist.getId() + ",\"name\":\"Playlist\",\"isPrivate\":false,\"ownerId\":\"mmuster\",\"songList\":" +
                            "[{\"id\":" + defaultSong1.getId() + ",\"title\":\"My humps\",\"artist\":\"Black Eyed Peas\",\"label\":\"anyLabel\",\"released\":2020}," +
                            "{\"id\":" + defaultSong2.getId() + ",\"title\":\"We Built This City\",\"artist\":\"Starship\",\"label\":\"Grunt/RCA\",\"released\":1985}]}]",
                    res.andReturn().getResponse().getContentAsString());
        }
    }

    @Test
    void getPlaylistNoPlaylistsBadTest() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.isJwtValid("BLOB")).thenReturn(true);
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT(anyString())).thenReturn(claim);
            when(claim.getId()).thenReturn("eschuler");

            when(playlistRepository.getAllPublicByOwnerId("mmuster")).thenReturn(new ArrayList<>(0));
            playlistMvc.perform(MockMvcRequestBuilders
                    .get("/songLists?userId=mmuster")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void getPlaylistByOwnerIdBadJWTBadTest() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.decodeJWT("BLOB")).thenThrow(IllegalArgumentException.class);

            when(playlistRepository.getPlaylistById(defaultPrivatePlaylist.getId())).thenReturn(defaultPrivatePlaylist);
            playlistMvc.perform(MockMvcRequestBuilders
                    .get("/songLists?userId=mmuster")
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isUnauthorized());
        }
    }

    //-------
    // POST
    //-------

    @Test
    @DisplayName("POST Playlist Good")
    void postPlaylistGoodTest() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT(anyString())).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            when(songRepository.findById(defaultSong1.getId())).thenReturn(java.util.Optional.ofNullable(defaultSong1));
            when(songRepository.findById(defaultSong2.getId())).thenReturn(java.util.Optional.ofNullable(defaultSong2));

            //Mock playlist id
            when(playlistRepository.save(any()).getId()).thenReturn(1);

            playlistMvc.perform(MockMvcRequestBuilders
                    .post("/songLists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\n" +
                            "    \"name\": \"Mmuster's Private Playlist\",\n" +
                            "    \"isPrivate\": true,\n" +
                            "    \"songList\": [\n" +
                            "        {\n" +
                            "            \"id\": " + defaultSong1.getId() + ",\n" +
                            "            \"title\": \"" + defaultSong1.getTitle() + "\",\n" +
                            "            \"artist\": \"" + defaultSong1.getArtist() + "\",\n" +
                            "            \"label\": \"" + defaultSong1.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong1.getReleased() + "\n" +
                            "        },\n" +
                            "        {\n" +
                            "            \"id\": " + defaultSong2.getId() + ",\n" +
                            "            \"title\": \"" + defaultSong2.getTitle() + "\",\n" +
                            "            \"artist\": \"" + defaultSong2.getArtist() + "\",\n" +
                            "            \"label\": \"" + defaultSong2.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong2.getReleased() + "\n" +
                            "        }\n" +
                            "    ]\n" +
                            "}")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    @DisplayName("POST Playlist Bad Token")
    void postPlaylistBadToken() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.decodeJWT("EXPIRED_TOKEN")).thenThrow(ExpiredJwtException.class);

            playlistMvc.perform(MockMvcRequestBuilders
                    .post("/songLists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\n" +
                            "    \"name\": \"Mmuster's Private Playlist\",\n" +
                            "    \"isPrivate\": true,\n" +
                            "    \"songList\": [\n" +
                            "        {\n" +
                            "            \"id\": " + defaultSong1.getId() + ",\n" +
                            "            \"title\": \"" + defaultSong1.getTitle() + "\",\n" +
                            "            \"artist\": \"" + defaultSong1.getArtist() + "\",\n" +
                            "            \"label\": \"" + defaultSong1.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong1.getReleased() + "\n" +
                            "        },\n" +
                            "        {\n" +
                            "            \"id\": " + defaultSong2.getId() + ",\n" +
                            "            \"title\": \"" + defaultSong2.getTitle() + "\",\n" +
                            "            \"artist\": \"" + defaultSong2.getArtist() + "\",\n" +
                            "            \"label\": \"" + defaultSong2.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong2.getReleased() + "\n" +
                            "        }\n" +
                            "    ]\n" +
                            "}")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "EXPIRED_TOKEN"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("POST Playlist with no name")
    void postPlaylistNoName() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT("BLOB")).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            playlistMvc.perform(MockMvcRequestBuilders
                    .post("/songLists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\n" +
                            "    \"isPrivate\": true,\n" +
                            "    \"songList\": [\n" +
                            "        {\n" +
                            "            \"id\": " + defaultSong1.getId() + ",\n" +
                            "            \"title\": \"" + defaultSong1.getTitle() + "\",\n" +
                            "            \"artist\": \"" + defaultSong1.getArtist() + "\",\n" +
                            "            \"label\": \"" + defaultSong1.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong1.getReleased() + "\n" +
                            "        },\n" +
                            "        {\n" +
                            "            \"id\": " + defaultSong2.getId() + ",\n" +
                            "            \"title\": \"" + defaultSong2.getTitle() + "\",\n" +
                            "            \"artist\": \"" + defaultSong2.getArtist() + "\",\n" +
                            "            \"label\": \"" + defaultSong2.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong2.getReleased() + "\n" +
                            "        }\n" +
                            "    ]\n" +
                            "}")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("POST Playlist Invalid Song")
    void postPlaylistNotExistingSong() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT(anyString())).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            when(songRepository.findById(defaultSong1.getId())).thenReturn(java.util.Optional.ofNullable(defaultSong1));
            when(songRepository.findById(defaultSong2.getId())).thenReturn(java.util.Optional.ofNullable(defaultSong2));

            //Mock playlist id
            when(playlistRepository.save(any()).getId()).thenReturn(1);

            playlistMvc.perform(MockMvcRequestBuilders
                    .post("/songLists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\n" +
                            "    \"name\": \"Mmuster's Private Playlist\",\n" +
                            "    \"isPrivate\": true,\n" +
                            "    \"songList\": [\n" +
                            "        {\n" +
                            "            \"id\": " + defaultSong1.getId() + ",\n" +
                            "            \"title\": \"" + defaultSong1.getTitle() + "\",\n" +
                            "            \"artist\": \"" + defaultSong1.getArtist() + "\",\n" +
                            "            \"label\": \"" + defaultSong1.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong1.getReleased() + "\n" +
                            "        },\n" +
                            "        {\n" +
                            "            \"id\": " + defaultSong2.getId() + ",\n" +
                            "            \"title\": \"" + defaultSong2.getTitle() + "\",\n" +
                            "            \"artist\": \"" + "Wrong Artist" + "\",\n" +
                            "            \"label\": \"" + defaultSong2.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong2.getReleased() + "\n" +
                            "        }\n" +
                            "    ]\n" +
                            "}")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("POST Playlist Not Existing Song")
    void postPlaylistNotExisitingSong() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT(anyString())).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            when(songRepository.findById(defaultSong1.getId())).thenReturn(java.util.Optional.ofNullable(defaultSong1));
            when(songRepository.findById(defaultSong2.getId())).thenReturn(java.util.Optional.ofNullable(defaultSong2));

            //Mock playlist id
            when(playlistRepository.save(any()).getId()).thenReturn(1);

            playlistMvc.perform(MockMvcRequestBuilders
                    .post("/songLists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\n" +
                            "    \"name\": \"Mmuster's Private Playlist\",\n" +
                            "    \"isPrivate\": true,\n" +
                            "    \"songList\": [\n" +
                            "        {\n" +
                            "            \"id\": " + 9999 + ",\n" +
                            "            \"title\": \"" + defaultSong1.getTitle() + "\",\n" +
                            "            \"artist\": \"" + defaultSong1.getArtist() + "\",\n" +
                            "            \"label\": \"" + defaultSong1.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong1.getReleased() + "\n" +
                            "        },\n" +
                            "        {\n" +
                            "            \"id\": " + defaultSong2.getId() + ",\n" +
                            "            \"title\": \"" + defaultSong2.getTitle() + "\",\n" +
                            "            \"artist\": \"" + defaultSong2.getArtist() + "\",\n" +
                            "            \"label\": \"" + defaultSong2.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong2.getReleased() + "\n" +
                            "        }\n" +
                            "    ]\n" +
                            "}")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isBadRequest());
        }
    }

    //--------------
    // PUT Playlist
    //--------------

    @Test
    @DisplayName("PUT Playlist Good Test")
    void putPlaylistGoodTest() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT("BLOB")).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            // Mock playlist Repo
            when(playlistRepository.getPlaylistById(defaultPublicPlaylist.getId())).thenReturn(defaultPublicPlaylist);
            // Mock Song Repo
            when(songRepository.findById(defaultSong2.getId())).thenReturn(java.util.Optional.ofNullable(defaultSong2));

            playlistMvc.perform(MockMvcRequestBuilders
                    .put("/songLists/" + defaultPublicPlaylist.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\n" +
                            "    \"id\": " + defaultPublicPlaylist.getId() + ", " +
                            "    \"name\": \"Mmuster's Public Playlist UPDATED\",\n" +
                            "    \"isPrivate\": false,\n" +
                            "    \"songList\": [\n" +
                            "        {\n" +
                            "            \"id\": " + defaultSong2.getId() + ",\n" +
                            "            \"title\": \"" + defaultSong2.getTitle() + "\",\n" +
                            "            \"artist\": \"" + defaultSong2.getArtist() + "\",\n" +
                            "            \"label\": \"" + defaultSong2.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong2.getReleased() + "\n" +
                            "        }" +
                            "    ]\n" +
                            "}")
                    .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNoContent());
        }
    }

    @Test
    @DisplayName("PUT Playlist not authorized")
    void goodTest() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            when(JwtDecode.decodeJWT("BLOB")).thenThrow(ExpiredJwtException.class);

            playlistMvc.perform(MockMvcRequestBuilders
                    .put("/songLists/" + defaultPublicPlaylist.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\n" +
                            "    \"id\": " + defaultPublicPlaylist.getId() + ", " +
                            "    \"name\": \"Mmuster's Public Playlist UPDATED\",\n" +
                            "    \"isPrivate\": false,\n" +
                            "    \"songList\": [\n" +
                            "        {\n" +
                            "            \"id\": " + defaultSong2.getId() + ",\n" +
                            "            \"title\": \"" + defaultSong2.getTitle() + "\",\n" +
                            "            \"artist\": \"" + defaultSong2.getArtist() + "\",\n" +
                            "            \"label\": \"" + defaultSong2.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong2.getReleased() + "\n" +
                            "        }" +
                            "    ]\n" +
                            "}")
                    .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("PUT Playlist different id in URL and payload")
    void putPlaylistDifferentId() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT("BLOB")).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            playlistMvc.perform(MockMvcRequestBuilders
                    .put("/songLists/" + 2)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\n" +
                            "    \"id\": " + defaultPublicPlaylist.getId() + ", " +
                            "    \"name\": \"Mmuster's Public Playlist UPDATED\",\n" +
                            "    \"isPrivate\": false,\n" +
                            "    \"songList\": [\n" +
                            "        {\n" +
                            "            \"id\": " + defaultSong2.getId() + ",\n" +
                            "            \"title\": \"" + defaultSong2.getTitle() + "\",\n" +
                            "            \"artist\": \"" + defaultSong2.getArtist() + "\",\n" +
                            "            \"label\": \"" + defaultSong2.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong2.getReleased() + "\n" +
                            "        }" +
                            "    ]\n" +
                            "}")
                    .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("PUT Playlist Not Owner")
    void putPlaylistNotOwner() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT("BLOB")).thenReturn(claim);
            when(claim.getId()).thenReturn("eschuler");

            // Mock playlist Repo
            when(playlistRepository.getPlaylistById(defaultPublicPlaylist.getId())).thenReturn(defaultPublicPlaylist);

            playlistMvc.perform(MockMvcRequestBuilders
                    .put("/songLists/" + defaultPublicPlaylist.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\n" +
                            "    \"id\": " + defaultPublicPlaylist.getId() + ", " +
                            "    \"name\": \"Mmuster's Public Playlist UPDATED\",\n" +
                            "    \"isPrivate\": false,\n" +
                            "    \"songList\": [\n" +
                            "        {\n" +
                            "            \"id\": " + defaultSong2.getId() + ",\n" +
                            "            \"title\": \"" + defaultSong2.getTitle() + "\",\n" +
                            "            \"artist\": \"" + defaultSong2.getArtist() + "\",\n" +
                            "            \"label\": \"" + defaultSong2.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong2.getReleased() + "\n" +
                            "        }" +
                            "    ]\n" +
                            "}")
                    .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("PUT Playlist Invalid Song")
    void putPlaylistInvalidSong() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT("BLOB")).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            // Mock playlist Repo
            when(playlistRepository.getPlaylistById(defaultPublicPlaylist.getId())).thenReturn(defaultPublicPlaylist);
            // Mock Song Repo
            when(songRepository.findById(defaultSong1.getId())).thenReturn(java.util.Optional.ofNullable(defaultSong1));

            playlistMvc.perform(MockMvcRequestBuilders
                    .put("/songLists/" + defaultPublicPlaylist.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\n" +
                            "    \"id\": " + defaultPublicPlaylist.getId() + ", " +
                            "    \"name\": \"Mmuster's Public Playlist UPDATED\",\n" +
                            "    \"isPrivate\": false,\n" +
                            "    \"songList\": [\n" +
                            "        {\n" +
                            "            \"id\": " + defaultSong1.getId() + ",\n" +
                            "            \"title\": \"" + "WRONG TITLE" + "\",\n" +
                            "            \"artist\": \"" + defaultSong1.getArtist() + "\",\n" +
                            "            \"label\": \"" + defaultSong1.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong1.getReleased() + "\n" +
                            "        }" +
                            "    ]\n" +
                            "}")
                    .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("PUT Playlist not existing Song id")
    void putPlaylistNotExistingSongId() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT("BLOB")).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            // Mock playlist Repo
            when(playlistRepository.getPlaylistById(defaultPublicPlaylist.getId())).thenReturn(defaultPublicPlaylist);

            playlistMvc.perform(MockMvcRequestBuilders
                    .put("/songLists/" + defaultPublicPlaylist.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\n" +
                            "    \"id\": " + defaultPublicPlaylist.getId() + ", " +
                            "    \"name\": \"Mmuster's Public Playlist UPDATED\",\n" +
                            "    \"isPrivate\": false,\n" +
                            "    \"songList\": [\n" +
                            "        {\n" +
                            "            \"id\": " + 99999 + ",\n" +
                            "            \"title\": \"" + defaultSong1.getTitle() + "\",\n" +
                            "            \"artist\": \"" + defaultSong1.getArtist() + "\",\n" +
                            "            \"label\": \"" + defaultSong1.getLabel() + "\",\n" +
                            "            \"released\": " + defaultSong1.getReleased() + "\n" +
                            "        }" +
                            "    ]\n" +
                            "}")
                    .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNotFound());
        }
    }


    //---------
    // DELETE
    //---------

    @Test
    @DisplayName("DELETE God Test")
    void deletePlaylistGoodTest() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT(anyString())).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            when(playlistRepository.getPlaylistById(defaultPrivatePlaylist.getId())).thenReturn(defaultPrivatePlaylist);
            playlistMvc.perform(MockMvcRequestBuilders
                    .delete("/songLists/" + defaultPrivatePlaylist.getId())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNoContent());

            verify(playlistRepository, atLeastOnce()).deleteById(defaultPrivatePlaylist.getId());
        }
    }

    @Test
    @DisplayName("DELETE bad Token")
    void deletePlaylistBadToken() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT("BAD_TOKEN")).thenThrow(UnsupportedJwtException.class);

            playlistMvc.perform(MockMvcRequestBuilders
                    .delete("/songLists/" + defaultPrivatePlaylist.getId())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "BAD_TOKEN"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("DELETE Not existing Playlist")
    void deletePlaylistBadPlaylist() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT(anyString())).thenReturn(claim);

            playlistMvc.perform(MockMvcRequestBuilders
                    .delete("/songLists/" + 99999)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNotFound());

            verify(playlistRepository, atMost(0)).deleteById(99999);
        }
    }

    @Test
    @DisplayName("DELETE Invalid Id")
    void deletePlaylistInvalidId() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT(anyString())).thenReturn(claim);

            playlistMvc.perform(MockMvcRequestBuilders
                    .delete("/songLists/" + 0)
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("DELETE playlist not authorized")
    void deletePlaylistUnauthorized() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT(anyString())).thenReturn(claim);
            when(claim.getId()).thenReturn("otherUser");

            when(playlistRepository.getPlaylistById(defaultPrivatePlaylist.getId())).thenReturn(defaultPrivatePlaylist);
            playlistMvc.perform(MockMvcRequestBuilders
                    .delete("/songLists/" + defaultPrivatePlaylist.getId())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isForbidden());

            verify(playlistRepository, atMost(0)).deleteById(defaultPrivatePlaylist.getId());
        }
    }

    @Test
    @DisplayName("DELETE check if delete is called")
    void deletePlaylistRandTest() throws Exception {
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            Claims claim = mock(Claims.class);
            when(JwtDecode.decodeJWT(anyString())).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            when(playlistRepository.getPlaylistById(defaultPublicPlaylist.getId())).thenReturn(defaultPublicPlaylist);
            playlistMvc.perform(MockMvcRequestBuilders
                    .delete("/songLists/" + defaultPublicPlaylist.getId())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNoContent());
            verify(playlistRepository, atLeastOnce()).deleteById(defaultPublicPlaylist.getId());
        }
    }

    @Test
    void playlistConstructor() {
        Playlist playlist = new Playlist("Test", true, "mmuster", Collections.emptyList());
        playlist.setOwnerId("eschuler");
        Assertions.assertEquals(playlist.getOwnerId(), "eschuler");
    }
}
