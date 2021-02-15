package htwb.ai.controller.controller;


import htwb.ai.controller.model.Concert;
import htwb.ai.controller.model.Song;
import htwb.ai.controller.repo.ConcertsRepository;
import htwb.ai.controller.utils.JwtDecode;
import org.apache.http.HttpException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author : Enrico de Chadarevian, Marvin Rausch
 * Project name : Marven
 * @version : 1.0
 * @since : 31-10-2020
 **/
@RestController
@RequestMapping(value = "/concerts")
public class ConcertsController {

    private final ConcertsRepository repo;

    public ConcertsController(ConcertsRepository repo) {
        this.repo = repo;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<Concert>> getConcerts(@RequestHeader(value = "Authorization", required = false) String jwt) {
        if (!JwtDecode.isJwtValid(jwt))
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        List<Concert> concertList = repo.getAllConcerts();
        if (concertList != null) {
            for (Concert concert : concertList) {
                concert.setSongList(getSongsFromArtist(concert.getArtist(), jwt));
            }
            return new ResponseEntity<>(concertList, HttpStatus.OK);
        } else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Concert> getConcert(@RequestHeader(value = "Authorization", required = false) String jwt,
                                              @PathVariable(value = "id") Long id) {
        if (!JwtDecode.isJwtValid(jwt))
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        if (id < 1) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Concert concert;
        try {
            concert = repo.findById(id).get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        concert.setSongList(getSongsFromArtist(concert.getArtist(), jwt));
        return new ResponseEntity<>(concert, HttpStatus.OK);
    }

    /**
     * Get all the songs made from the given artist.
     * This will make a request to song service.
     *
     * @param artist Artist to be searched
     * @return Songs made from given artist, will be empty if none found
     */
    private List<Song> getSongsFromArtist(String artist, String jwt) {
        Song[] songs = new Song[0];
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, jwt);
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

            HttpEntity<String> entity = new HttpEntity<>(null, headers);

            ResponseEntity<Song[]> response = restTemplate.exchange(
                    "http://" + InetAddress.getLocalHost().getHostAddress() + ":8080/songsWS/rest/songs?artist=" + artist,
                    HttpMethod.GET, entity, Song[].class);
            songs = response.getBody();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return Arrays.asList(songs.clone());
    }
}
