package htwb.ai.controller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import htwb.ai.controller.utils.JwtUtils;
import htwb.ai.controller.model.Song;
import htwb.ai.controller.repo.SongRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author : Enrico de Chadarevian, Marvin Rausch
 * Project name : Marven
 * @version : 1.0
 * @since : 31-10-2020
 **/
@RestController
@RequestMapping(value = "/songs")
public class SongController {

    private final SongRepository repo;

    public SongController(SongRepository repo) {
        this.repo = repo;
    }

    /**
     * GET http://localhost:8080/rest/songs/{songId}
     * Get a user from id
     *
     * @param id user id
     * @return response
     */
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Song> getSong(@RequestHeader("Authorization") String jwt, @PathVariable(value = "id") Integer id) {
        if (!JwtUtils.verifyJWT(jwt))
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        if (id < 1) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Song song = repo.findSongBySongId(id);
        if (song != null)
            return new ResponseEntity<>(song, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * GET http://localhost:8080/rest/songs/
     * Get all users
     *
     * @return response
     */
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<Song>> getAllSongs(@RequestHeader("Authorization") String jwt) {
        if (!JwtUtils.verifyJWT(jwt))
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        List<Song> songs = repo.getAllSongs();
        return new ResponseEntity<>(songs, HttpStatus.OK);
    }

    /**
     * POST http://localhost:8080/rest/songs/
     * Add a song
     * Accepts JSON
     *
     * @param song Song
     * @return response
     * @throws URISyntaxException URISyntaxException
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addSong(@RequestHeader("Authorization") String jwt, @RequestBody Song song) throws URISyntaxException {
        if (!JwtUtils.verifyJWT(jwt))
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        if (song.getTitle() == null || song.getId() != null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        int newId;
        try {
            Song newSong = repo.save(song);
            newId = newSong.getId();
        } catch (PersistenceException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Location Header
        HttpHeaders responseHeaders = new HttpHeaders();
        URI locationURI = new URI(String.format("/rest/songs/%d", newId));
        return ResponseEntity.created(locationURI).headers(responseHeaders).body(null);
    }

    /**
     * POST http://localhost:8080/rest/songs/
     * Add a song
     * Accepts JSON File
     *
     * @param jwt      JWT
     * @param jsonFile Multipart Json File
     * @return song Song
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addSongFile(@RequestHeader("Authorization") String jwt, @RequestParam("file") MultipartFile jsonFile) {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStreamReader inputStreamReader = new InputStreamReader(jsonFile.getInputStream())) {
            Song song = objectMapper.readValue(inputStreamReader, Song.class);

            return addSong(jwt, song);
        } catch (IOException | URISyntaxException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT http://localhost:8080/rest/songs/{songId}
     * Update an existing song
     * Accepts JSON
     *
     * @param song Song
     * @param id   id
     * @return response
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> editSong(@RequestHeader("Authorization") String jwt,
                                           @RequestBody Song song, @PathVariable(value = "id") Integer id) {
        if (!JwtUtils.verifyJWT(jwt))
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        try {
            if (song.getTitle() == null || !id.equals(song.getId())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (NullPointerException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!repo.existsById(id))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        try {
            Song songToEdit = repo.findSongBySongId(id);

            songToEdit.setTitle(song.getTitle());
            songToEdit.setArtist(song.getArtist());
            songToEdit.setLabel(song.getLabel());
            songToEdit.setReleased(song.getReleased());

            repo.save(songToEdit);
        } catch (NullPointerException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * DELETE http://localhost:8080/rest/songs/{songId}
     * Delete a song
     *
     * @param id id
     * @return response
     */
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteSong(@RequestHeader("Authorization") String jwt, @PathVariable(value = "id") Integer id) {
        if (!JwtUtils.verifyJWT(jwt))
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        if (id < 1) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            repo.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}




