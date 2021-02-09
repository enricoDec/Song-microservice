package integration;

import htwb.ai.controller.AuthController;
import htwb.ai.controller.PlaylistController;
import htwb.ai.model.Playlist;
import htwb.ai.model.Song;
import htwb.ai.repo.PlaylistRepository;
import htwb.ai.repo.SongRepository;
import htwb.ai.repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author : Enrico Gamil Toros
 * Project name : MarvEn
 * @version : 1.0
 * @since : 19.01.21
 **/

@SpringBootTest
@TestPropertySource(locations = "/test.properties")
@ExtendWith(SystemStubsExtension.class)
public class PlaylistTest {
    private MockMvc playlistMvc;
    private String mmusterToken;
    private String user2Token;
    private Song defaultSong;
    @SystemStub
    private EnvironmentVariables environment = new EnvironmentVariables("SECRET_KEY_KBE", "test_secret_key");
    @Autowired
    private PlaylistRepository playlistRepository;
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setupMockMvc() throws Exception {
        defaultSong = songRepository.save(new Song("My humps", "Black Eyed Peas", "anyLabel", 2019));
        String mmusterAuth = "{\"userId\": \"mmuster\" , \"password\": \"pass1234\"}";
        String user2Auth = "{\"userId\": \"admin\" , \"password\": \"admin\"}";

        playlistMvc = MockMvcBuilders.standaloneSetup(new PlaylistController(playlistRepository, songRepository)).build();
        MockMvc authMockMvc = MockMvcBuilders.standaloneSetup(new AuthController(userRepository)).build();

        mmusterToken = authMockMvc.perform(
                post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mmusterAuth))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        user2Token = authMockMvc.perform(
                post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user2Auth))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    @AfterEach
    public void teardown() {
        playlistRepository.deleteAll();
        songRepository.deleteAll();
    }

    @Test
    public void addSongListsGood() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(java.net.http.HttpHeaders.AUTHORIZATION, mmusterToken).content("{\"isPrivate\": true,\"name\": \"mmusterTest\",\"songList\": [{\"id\": " + defaultSong.getId() + ",\"title\": \"My humps\",\"artist\": \"Black Eyed Peas\",\"label\": \"anyLabel\",\"released\": 2019}]}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void addSongListsGoodCheckDBEntry() throws Exception {
        ResultActions resultActions = playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(java.net.http.HttpHeaders.AUTHORIZATION, mmusterToken).content("{\"isPrivate\": true,\"name\": \"mmusterTest\",\"songList\": [{\"id\": " + defaultSong.getId() + ",\"title\": \"My humps\",\"artist\": \"Black Eyed Peas\",\"label\": \"anyLabel\",\"released\": 2019}]}"))
                .andExpect(status().isCreated());

        Playlist playlist = playlistRepository.getPlaylistById(getSongListId(resultActions));
        Assertions.assertEquals("mmuster", playlist.getOwnerId());
    }

    @Test
    public void addPlaylistBadEmptySongList() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, mmusterToken).content("{\"isPrivate\": true,\"name\": \"mmusterTest\",\"songList\": []}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addSongListBadSongInPlaylistDoesNotExistInDB() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, mmusterToken).content("{\"isPrivate\": true,\"name\": \"mmusterTest\",\"songList\": [{\"id\": 1,\"title\": \"Can’ht Stop the Feeling\",\"artist\": \"Justin Timberlake\",\"label\": \"Trolls\",\"released\": 2016}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addSongListsBadPrivateValueMissing() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, mmusterToken).content("{\"name\": \"mmusterTest\",\"songList\": [{\"id\": " + defaultSong.getId() + ",\"title\": \"My humps\",\"artist\": \"Black Eyed Peas\",\"label\": \"anyLabel\",\"released\": 2019}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getSongListGoodJSON() throws Exception {
        ResultActions result = playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, mmusterToken).content("{\"isPrivate\": true,\"name\": \"mmusterTest\",\"songList\": [{\"id\": " + defaultSong.getId() + ",\"title\": \"My humps\",\"artist\": \"Black Eyed Peas\",\"label\": \"anyLabel\",\"released\": 2019}]}"))
                .andExpect(status().isCreated());

        int playlistId = getSongListId(result);

        playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists?userId=mmuster")
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, mmusterToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(playlistId))
                .andExpect(jsonPath("$[0].name").value("mmusterTest"))
                .andExpect(jsonPath("$[0].isPrivate").value(true))
                .andExpect(jsonPath("$[0].ownerId").value("mmuster"))
                .andExpect(jsonPath("$[0]['songList'][0]['id']").value(defaultSong.getId()))
                .andExpect(jsonPath("$[0]['songList'][0]['title']").value("My humps"))
                .andExpect(jsonPath("$[0]['songList'][0]['artist']").value("Black Eyed Peas"))
                .andExpect(jsonPath("$[0]['songList'][0]['label']").value("anyLabel"))
                .andExpect(jsonPath("$[0]['songList'][0]['released']").value(2019));
    }

    @Test
    public void getSongListGoodXML() throws Exception {
        ResultActions result = playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, mmusterToken).content("{\"isPrivate\": true,\"name\": \"mmusterTest\",\"songList\": [{\"id\": " + defaultSong.getId() + ",\"title\": \"My humps\",\"artist\": \"Black Eyed Peas\",\"label\": \"anyLabel\",\"released\": 2019}]}"))
                .andExpect(status().isCreated());

        int playlistId = getSongListId(result);

        ResultActions resultActions = playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists?userId=mmuster")
                .accept(MediaType.APPLICATION_XML).
                        header(HttpHeaders.AUTHORIZATION, mmusterToken))
                .andExpect(status().isOk());

        Assertions.assertEquals("<List><item><id>" + playlistId + "</id><name>mmusterTest</name>" +
                "<isPrivate>true</isPrivate>" +
                "<ownerId>mmuster</ownerId><songList><songList><id>" + defaultSong.getId() + "</id>" +
                "<title>My humps</title>" +
                "<artist>Black Eyed Peas</artist><label>anyLabel</label><released>2019</released>" +
                "</songList></songList></item></List>", resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void getSongListByIdGoodJSON() throws Exception {
        ResultActions result = playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, mmusterToken).content("{\"isPrivate\": true,\"name\": \"mmusterTest\",\"songList\": [{\"id\": " + defaultSong.getId() + ",\"title\": \"My humps\",\"artist\": \"Black Eyed Peas\",\"label\": \"anyLabel\",\"released\": 2019}]}"))
                .andExpect(status().isCreated());

        int playlistId = getSongListId(result);

        playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists/" + playlistId)
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, mmusterToken))
                .andExpect(status().isOk()).andExpect(jsonPath("['id']").value(playlistId))
                .andExpect(jsonPath("['name']").value("mmusterTest"))
                .andExpect(jsonPath("['isPrivate']").value(true))
                .andExpect(jsonPath("['ownerId']").value("mmuster"))
                .andExpect(jsonPath("['songList'][0]['id']").value(defaultSong.getId()))
                .andExpect(jsonPath("['songList'][0]['title']").value("My humps"))
                .andExpect(jsonPath("['songList'][0]['artist']").value("Black Eyed Peas"))
                .andExpect(jsonPath("['songList'][0]['label']").value("anyLabel"))
                .andExpect(jsonPath("['songList'][0]['released']").value(2019));
    }

    @Test
    public void getSongListByIdGoodXML() throws Exception {
        ResultActions result = playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, mmusterToken).content("{\"isPrivate\": true,\"name\": \"mmusterTest\",\"songList\": [{\"id\": " + defaultSong.getId() + ",\"title\": \"My humps\",\"artist\": \"Black Eyed Peas\",\"label\": \"anyLabel\",\"released\": 2019}]}"))
                .andExpect(status().isCreated());

        int playlistId = getSongListId(result);

        ResultActions resultActions = playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists/" + playlistId)
                .accept(MediaType.APPLICATION_XML).
                        header(HttpHeaders.AUTHORIZATION, mmusterToken))
                .andExpect(status().isOk());

        Assertions.assertEquals("<Playlist><id>" + playlistId + "</id><name>mmusterTest</name><isPrivate>true</isPrivate>" +
                "<ownerId>mmuster</ownerId><songList><songList><id>" + defaultSong.getId() + "</id><title>My humps</title>" +
                "<artist>Black Eyed Peas</artist><label>anyLabel</label><released>2019" +
                "</released></songList></songList></Playlist>", resultActions.
                andReturn().getResponse().getContentAsString());
    }

    @Test
    public void getSongListBadPrivatePlaylistOfOtherUser() throws Exception {
        ResultActions result = playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, mmusterToken).content("{\"isPrivate\": true,\"name\": \"mmusterTest\",\"songList\": [{\"id\": " + defaultSong.getId() + ",\"title\": \"My humps\",\"artist\": \"Black Eyed Peas\",\"label\": \"anyLabel\",\"released\": 2019}]}"))
                .andExpect(status().isCreated());

        int playlistId = getSongListId(result);

        playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists/" + playlistId)
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, user2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getSongListGoodPublicPlaylistOfOtherUser() throws Exception {
        ResultActions result = playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, mmusterToken).content("{\"isPrivate\": false,\"name\": \"mmusterTest\",\"songList\": [{\"id\": " + defaultSong.getId() + ",\"title\": \"My humps\",\"artist\": \"Black Eyed Peas\",\"label\": \"anyLabel\",\"released\": 2019}]}"))
                .andExpect(status().isCreated());

        int playlistId = getSongListId(result);

        playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists/" + playlistId)
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, user2Token))
                .andExpect(status().isOk());
    }

    @Test
    public void getSongListGoodOnlyPublicPlaylistOfOtherUser() throws Exception {
        ResultActions result = playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, mmusterToken).content("{\"isPrivate\": false,\"name\": \"mmusterTest\",\"songList\": [{\"id\": " + defaultSong.getId() + ",\"title\": \"My humps\",\"artist\": \"Black Eyed Peas\",\"label\": \"anyLabel\",\"released\": 2019}]}"))
                .andExpect(status().isCreated());

        int publicPlaylistId = getSongListId(result);

        playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, mmusterToken).content("{\"isPrivate\": true,\"name\": \"mmusterTest2\",\"songList\": [{\"id\": " + defaultSong.getId() + ",\"title\": \"My humps\",\"artist\": \"Black Eyed Peas\",\"label\": \"anyLabel\",\"released\": 2019}]}"))
                .andExpect(status().isCreated());

        ResultActions resultGet = playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists?userId=mmuster")
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, user2Token))
                .andExpect(status().isOk());

        Assertions.assertEquals("[{\"id\":"+publicPlaylistId+",\"name\":\"mmusterTest\",\"isPrivate\":false," +
                "\"ownerId\":\"mmuster\",\"songList\":[{\"id\":"+defaultSong.getId()+",\"title\":\"My humps\"," +
                "\"artist\":\"Black Eyed Peas\",\"label\":\"anyLabel\"," +
                "\"released\":2019}]}]", resultGet.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void getSongListByIdBadIdNotExisting() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists/999999")
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, mmusterToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getSongListByIdBadInvalidId() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists/0")
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, mmusterToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getSongListByIdBadInvalidIdNoNumber() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists/abc")
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, mmusterToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getSongListBadNoPlaylistCreatedForUser() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists?userId=mmuster")
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, mmusterToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getSongListBadUserDoesNotExist() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists?userId=exampleUser")
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, mmusterToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteSongListGood() throws Exception {
        ResultActions resultActions = playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, mmusterToken).content("{\"isPrivate\": true,\"name\": \"mmusterTest\",\"songList\": [{\"id\": " + defaultSong.getId() + ",\"title\": \"My humps\",\"artist\": \"Black Eyed Peas\",\"label\": \"anyLabel\",\"released\": 2019}]}"))
                .andExpect(status().isCreated());
        int playlistId = getSongListId(resultActions);

        playlistMvc.perform(MockMvcRequestBuilders
                .delete("/songLists/" + playlistId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, mmusterToken))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteSongListBadSongListIdNotExisting() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .delete("/songLists/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, mmusterToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteSongListBadPlaylistOfOtherUser() throws Exception {
        ResultActions resultActions = playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, mmusterToken).content("{\"isPrivate\": false,\"name\": \"mmusterTest\",\"songList\": [{\"id\": " + defaultSong.getId() + ",\"title\": \"My humps\",\"artist\": \"Black Eyed Peas\",\"label\": \"anyLabel\",\"released\": 2019}]}"))
                .andExpect(status().isCreated());
        int playlistId = getSongListId(resultActions);

        playlistMvc.perform(MockMvcRequestBuilders
                .delete("/songLists/" + playlistId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, user2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    public void postPlaylistAuthorizationBadEmptyToken() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "    ").content("{\"isPrivate\": true,\"name\": \"mmusterTest\",\"songList\": [{\"id\": " + defaultSong.getId() + ",\"title\": \"My humps\",\"artist\": \"Black Eyed Peas\",\"label\": \"anyLabel\",\"released\": 2019}]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void postPlaylistAuthorizationBadWrongToken() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .post("/songLists")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "frejhgofherfoig").content("{\"isPrivate\": true,\"name\": \"mmusterTest\",\"songList\": [{\"id\": " + defaultSong.getId() + ",\"title\": \"My humps\",\"artist\": \"Black Eyed Peas\",\"label\": \"anyLabel\",\"released\": 2019}]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getPlaylistByUserIdAuthorizationBadEmptyToken() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists?userId=mmuster")
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, "   "))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getPlaylistByUserIdAuthorizationBadWrongToken() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists?userId=mmuster")
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, "fnerwujrfgo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getPlaylistByIdAuthorizationBadEmptyToken() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists/1")
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, "     "))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getPlaylistByIdAuthorizationBadWrongToken() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .get("/songLists/1")
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, "gtfhbtrh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deletePlaylistByIdAuthorizationBadEmptyToken() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .delete("/songLists/1")
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, "       "))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deletePlaylistByIdAuthorizationBadWrongToken() throws Exception {
        playlistMvc.perform(MockMvcRequestBuilders
                .delete("/songLists/1")
                .accept(MediaType.APPLICATION_JSON).
                        header(HttpHeaders.AUTHORIZATION, "gtfhbtrh"))
                .andExpect(status().isUnauthorized());
    }

    private int getSongListId(ResultActions resultActions) {
        String locationHeader = resultActions.andReturn().getResponse().getHeaders(HttpHeaders.LOCATION).get(0);
        return Integer.parseInt(locationHeader.substring("/rest/songLists/".length()));
    }
}
