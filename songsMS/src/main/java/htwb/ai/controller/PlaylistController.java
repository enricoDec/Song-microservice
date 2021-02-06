package htwb.ai.controller;

import htwb.ai.utils.JwtUtils;
import htwb.ai.model.Playlist;
import htwb.ai.model.Song;
import htwb.ai.repo.PlaylistRepository;
import htwb.ai.repo.SongRepository;
import io.jsonwebtoken.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.PersistenceException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author : Enrico de Chadarevian, Marvin Rausch
 * Project name : Marven
 * @version : 1.0
 * @since : 22-12-2020
 **/
@RestController
@RequestMapping(value = "/songLists")
public class PlaylistController {
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;


    public PlaylistController(PlaylistRepository repo, SongRepository repo2) {
        this.playlistRepository = repo;
        this.songRepository = repo2;
    }


    /**
     * Get a playlist by userId parameter, if userId is same
     * as authorized user get all playlists else only public Playlists
     * Authorization: token
     * Accept: application/json
     * <p>
     * Example:
     * GET ../songsWS-MarvEn/rest/songLists?userId=mmuster
     *
     * @param jwt  JWT Token
     * @param user User id of the requested playlist
     * @return Playlist and http 200 if request successful
     */
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<Playlist>> getAllAuthorizedPlaylist(@RequestHeader("Authorization") String jwt,
                                                                   @RequestParam(value = "userId") String user) {
        if (user.isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        //JWT
        Claims claims;
        try {
            claims = JwtUtils.decodeJWT(jwt);
        } catch (ExpiredJwtException | UnsupportedJwtException |
                MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        //If authenticated user request his own playlists
        if (claims.getId().equals(user)) {
            List<Playlist> playlists = playlistRepository.getAllByOwnerId(user);
            //if no playlist
            if (playlists.size() == 0)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(playlists, HttpStatus.OK);
        } else {
            //Else only return public playlists
            List<Playlist> playlists = playlistRepository.getAllPublicByOwnerId(user);
            if (playlists.size() == 0)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(playlists, HttpStatus.OK);
        }
    }

    /**
     * Get a Playlist by playlist Id, only if authorized user is the
     * owner of the requested playlist or the playlist is public
     * <p>
     * Authorization: token
     * Accept: application/json
     * <p>
     * Example:
     * GET ../songsWS-MarvEn/rest/songLists/5
     *
     * @param jwt        Jwt token
     * @param playlistId Id of the requested playlist
     * @return requested playlist with corresponding songs
     */
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Playlist> getPlaylistById(@RequestHeader("Authorization") String jwt,
                                                    @PathVariable(value = "id") Integer playlistId) {
        if (playlistId == 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Claims claims;
        try {
            claims = JwtUtils.decodeJWT(jwt);
        } catch (ExpiredJwtException | UnsupportedJwtException |
                MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        //If authenticated user request his playlists
        Playlist requestedPlaylist = playlistRepository.getPlaylistById(playlistId);
        if (requestedPlaylist == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (claims.getId().equals(requestedPlaylist.getOwnerId()) || !requestedPlaylist.getIsPrivate()) {
            return new ResponseEntity<>(requestedPlaylist, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Post a new playlist, owner of the playlist will be the authenticated user sending the post request
     * <p>
     * Content-Type: application/json
     * Headers
     * Authorization: token
     * Content-Type: application/json
     * <p>
     * Example:
     * POST ../rest/songLists
     *
     * @param jwt      Jwt
     * @param playlist Playlist to add
     * @return Location Header with Id of the new Playlist
     * @throws URISyntaxException URISyntaxException
     */
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> addPlaylist(@RequestHeader("Authorization") String jwt, @RequestBody Playlist playlist) throws URISyntaxException {
        Claims claims;
        try {
            claims = JwtUtils.decodeJWT(jwt);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        //Validate playlist
        if (playlist.getOwnerId() != null || playlist.getId() != null || playlist.getName() == null
                || playlist.getIsPrivate() == null || playlist.getSongList().isEmpty())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        int newId;
        try {
            Playlist newPlaylist = new Playlist(playlist.getName(), playlist.getIsPrivate(), claims.getId());
            // Make new Playlist get owner from jwt
            for (Song song : playlist.getSongList()) {
                Song compareSong = songRepository.findById(song.getId()).get();
                newPlaylist.addSong(compareSong);
                if (!compareSong.getArtist().equals(song.getArtist())
                        || !compareSong.getTitle().equals(song.getTitle())
                        || !compareSong.getReleased().equals(song.getReleased())
                        || !compareSong.getLabel().equals(song.getLabel())) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            newId = playlistRepository.save(newPlaylist).getId();
        } catch (PersistenceException | NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Location Header
        HttpHeaders responseHeaders = new HttpHeaders();
        URI locationURI = new URI(String.format("/rest/songLists/%d", newId));
        return ResponseEntity.created(locationURI).headers(responseHeaders).body(null);
    }


    /**
     * Delete a specific Playlist
     * Only authorized owner of playlist can delete his own playlists
     * Example:
     * Delete ..
     *
     * @param jwt        Jwt token
     * @param playlistId id of playlist to be deleted
     * @return HTTP NO CONTENT if playlist has been deleted, UNAUTHORIZED if not authorized,
     * FORBIDDEN if trying to delete playlist that belongs to another user or BAD REQUEST
     */
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deletePlaylistById(@RequestHeader("Authorization") String jwt,
                                                     @PathVariable(value = "id") Integer playlistId) {
        if (playlistId == 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Claims claims;
        try {
            claims = JwtUtils.decodeJWT(jwt);
        } catch (ExpiredJwtException | UnsupportedJwtException |
                MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        //If authenticated user request to delete his playlists
        Playlist requestedPlaylist = playlistRepository.getPlaylistById(playlistId);
        if (requestedPlaylist == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (claims.getId().equals(requestedPlaylist.getOwnerId())) {
            playlistRepository.deleteById(playlistId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
