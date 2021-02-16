package htwb.ai.controller.controller;


import htwb.ai.controller.model.Concert;
import htwb.ai.controller.model.ConcertData;
import htwb.ai.controller.repo.ConcertsRepository;
import htwb.ai.controller.utils.JwtDecode;
import htwb.ai.controller.utils.RequestUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.PersistenceException;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collections;
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
    private final RequestUtils requestUtils;

    public ConcertsController(ConcertsRepository repo, RequestUtils requestUtils) {
        this.repo = repo;
        this.requestUtils = requestUtils;
    }

    /**
     * Get all concerts with songs played by artist
     * Example:
     * Get ../songWS/rest/concerts
     *
     * @param jwt Jwt Token
     * @return list of all concerts
     */
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<Concert>> getConcerts(@RequestHeader(value = "Authorization", required = false) String jwt) {
        if (!JwtDecode.isJwtValid(jwt))
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        List<Concert> concertList = repo.getAllConcerts();
        if (!concertList.isEmpty()) {
            for (Concert concert : concertList) {
                try {
                    concert.setSongList(requestUtils.getSongsFromArtist(concert.getArtist(), jwt));
                } catch (UnknownHostException | HttpClientErrorException | NoSuchElementException e) {
                    concert.setSongList(Collections.emptyList());
                }
            }
            return new ResponseEntity<>(concertList, HttpStatus.OK);
        } else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Get specific concert by id with songs played by artist
     * Example:
     * Get ../songWS/rest/concerts/{id}
     *
     * @param jwt Jwt Token
     * @param id  id of concert to get
     * @return Requested concert or 404 if not found
     */
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
        try {
            concert.setSongList(requestUtils.getSongsFromArtist(concert.getArtist(), jwt));
        } catch (UnknownHostException | HttpClientErrorException | NoSuchElementException e) {
            concert.setSongList(Collections.emptyList());
        }
        return new ResponseEntity<>(concert, HttpStatus.OK);
    }

    /**
     * Post a new concert
     * <p>
     * Content-Type: application/json
     * Headers:
     * Authorization: token
     * <p>
     * Example:
     * POST ../rest/concerts/
     *
     * @param jwt     Jwt
     * @param concert concert to add
     * @return Location Header with Id of the new Playlist
     * @throws URISyntaxException URISyntaxException
     */
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> addConcert(@RequestHeader(value = "Authorization", required = false) String jwt,
                                             @RequestBody @Valid ConcertData concert) throws URISyntaxException {
        if (!JwtDecode.isJwtValid(jwt))
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        Long newId;
        try {
            Concert newConcert = new Concert(concert.getLocation(), concert.getArtist(), concert.getMaxTickets());
            newId = repo.save(newConcert).getConcertId();
        } catch (PersistenceException | NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Location Header
        HttpHeaders responseHeaders = new HttpHeaders();
        URI locationURI = new URI(String.format("/rest/concerts/%d", newId));
        return ResponseEntity.created(locationURI).headers(responseHeaders).body(null);
    }
}
