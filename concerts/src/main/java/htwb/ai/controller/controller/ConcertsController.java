package htwb.ai.controller.controller;


import com.netflix.discovery.converters.Auto;
import htwb.ai.controller.model.Concert;
import htwb.ai.controller.repo.ConcertsRepository;
import htwb.ai.controller.utils.JwtDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        List<Concert> concertList = repo.getAll();
        if (concertList != null)
            return new ResponseEntity<>(concertList, HttpStatus.OK);
        else
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
        return new ResponseEntity<>(concert, HttpStatus.OK);
    }
}
