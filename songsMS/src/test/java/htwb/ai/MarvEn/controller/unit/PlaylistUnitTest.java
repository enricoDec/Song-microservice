package htwb.ai.MarvEn.controller.unit;

import htwb.ai.MarvEn.utils.JwtUtils;
import htwb.ai.MarvEn.controller.PlaylistController;
import htwb.ai.MarvEn.model.Playlist;
import htwb.ai.MarvEn.model.Song;
import htwb.ai.MarvEn.repo.PlaylistRepository;
import htwb.ai.MarvEn.repo.SongRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
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
    private EnvironmentVariables environment = new EnvironmentVariables("SECRET_KEY_KBE", "test_secret_key");
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

    @Test
    void getPlaylistByIdGoodTest() throws Exception {
        try (MockedStatic<JwtUtils> jwtUtilsMockedStatic = mockStatic(JwtUtils.class)) {
            Claims claim = mock(Claims.class);
            when(JwtUtils.decodeJWT("BLOB")).thenReturn(claim);
            when(claim.getId()).thenReturn("otherOwner");

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
        try (MockedStatic<JwtUtils> jwtUtilsMockedStatic = mockStatic(JwtUtils.class)) {
            int notExisitingPlaylistId = 9999;
            Claims claim = mock(Claims.class);
            when(JwtUtils.decodeJWT("BLOB")).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            when(playlistRepository.getPlaylistById(notExisitingPlaylistId)).thenReturn(null);
            playlistMvc.perform(MockMvcRequestBuilders
                    .get("/songLists/" + notExisitingPlaylistId)
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void getPrivatePlaylistByIdNotOwnerBadTest() throws Exception {
        try (MockedStatic<JwtUtils> jwtUtilsMockedStatic = mockStatic(JwtUtils.class)) {
            Claims claim = mock(Claims.class);
            when(JwtUtils.decodeJWT("BLOB")).thenReturn(claim);
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
        try (MockedStatic<JwtUtils> jwtUtilsMockedStatic = mockStatic(JwtUtils.class)) {
            when(JwtUtils.decodeJWT("BLOB")).thenThrow(IllegalArgumentException.class);

            when(playlistRepository.getPlaylistById(defaultPrivatePlaylist.getId())).thenReturn(defaultPrivatePlaylist);
            playlistMvc.perform(MockMvcRequestBuilders
                    .get("/songLists/" + defaultPrivatePlaylist.getId())
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void getPlaylistByOwnerGoodTest() throws Exception {
        try (MockedStatic<JwtUtils> jwtUtilsMockedStatic = mockStatic(JwtUtils.class)) {
            when(JwtUtils.verifyJWT("BLOB")).thenReturn(true);
            Claims claim = mock(Claims.class);
            when(JwtUtils.decodeJWT(anyString())).thenReturn(claim);
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
    void getPlaylistByNotOwnerGoodTest() throws Exception {
        try (MockedStatic<JwtUtils> jwtUtilsMockedStatic = mockStatic(JwtUtils.class)) {
            when(JwtUtils.verifyJWT("BLOB")).thenReturn(true);
            Claims claim = mock(Claims.class);
            when(JwtUtils.decodeJWT(anyString())).thenReturn(claim);
            when(claim.getId()).thenReturn("eschuler");

            when(playlistRepository.getAllPublicByOwnerId("mmuster")).thenReturn(Arrays.asList(defaultPublicPlaylist));
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
        try (MockedStatic<JwtUtils> jwtUtilsMockedStatic = mockStatic(JwtUtils.class)) {
            when(JwtUtils.verifyJWT("BLOB")).thenReturn(true);
            Claims claim = mock(Claims.class);
            when(JwtUtils.decodeJWT(anyString())).thenReturn(claim);
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
        try (MockedStatic<JwtUtils> jwtUtilsMockedStatic = mockStatic(JwtUtils.class)) {
            when(JwtUtils.decodeJWT("BLOB")).thenThrow(IllegalArgumentException.class);

            when(playlistRepository.getPlaylistById(defaultPrivatePlaylist.getId())).thenReturn(defaultPrivatePlaylist);
            playlistMvc.perform(MockMvcRequestBuilders
                    .get("/songLists?userId=mmuster")
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void deletePlaylistGoodTest() throws Exception {
        try (MockedStatic<JwtUtils> jwtUtilsMockedStatic = mockStatic(JwtUtils.class)) {
            when(JwtUtils.verifyJWT("BLOB")).thenReturn(true);
            Claims claim = mock(Claims.class);
            when(JwtUtils.decodeJWT(anyString())).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            when(playlistRepository.getPlaylistById(defaultPrivatePlaylist.getId())).thenReturn(defaultPrivatePlaylist);
            playlistMvc.perform(MockMvcRequestBuilders
                    .delete("/songLists/" + defaultPrivatePlaylist.getId())
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNoContent());

            verify(playlistRepository, atLeastOnce()).deleteById(defaultPrivatePlaylist.getId());
        }
    }

    @Test
    void deletePlaylistBadTest() throws Exception {
        try (MockedStatic<JwtUtils> jwtUtilsMockedStatic = mockStatic(JwtUtils.class)) {
            when(JwtUtils.verifyJWT("BLOB")).thenReturn(true);
            Claims claim = mock(Claims.class);
            when(JwtUtils.decodeJWT(anyString())).thenReturn(claim);
            when(claim.getId()).thenReturn("otherUser");

            when(playlistRepository.getPlaylistById(defaultPrivatePlaylist.getId())).thenReturn(defaultPrivatePlaylist);
            playlistMvc.perform(MockMvcRequestBuilders
                    .delete("/songLists/" + defaultPrivatePlaylist.getId())
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isForbidden());

            verify(playlistRepository, atMost(0)).deleteById(defaultPrivatePlaylist.getId());
        }
    }

    @Test
    void deletePlaylistRandTest() throws Exception {
        try (MockedStatic<JwtUtils> jwtUtilsMockedStatic = mockStatic(JwtUtils.class)) {
            when(JwtUtils.verifyJWT("BLOB")).thenReturn(true);
            Claims claim = mock(Claims.class);
            when(JwtUtils.decodeJWT(anyString())).thenReturn(claim);
            when(claim.getId()).thenReturn("mmuster");

            when(playlistRepository.getPlaylistById(defaultPublicPlaylist.getId())).thenReturn(defaultPublicPlaylist);
            playlistMvc.perform(MockMvcRequestBuilders
                    .delete("/songLists/" + defaultPublicPlaylist.getId())
                    .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "BLOB"))
                    .andExpect(status().isNoContent());
            verify(playlistRepository, atLeastOnce()).deleteById(defaultPublicPlaylist.getId());
        }
    }

    @Test
    @DisplayName("POST Playlist Good")
    void postPlaylistGoodTest() throws Exception {
        try (MockedStatic<JwtUtils> jwtUtilsMockedStatic = mockStatic(JwtUtils.class)) {
            when(JwtUtils.verifyJWT(anyString())).thenReturn(true);
            Claims claim = mock(Claims.class);
            when(JwtUtils.decodeJWT(anyString())).thenReturn(claim);
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

}
