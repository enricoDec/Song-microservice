package htwb.ai.controller.controller;


import htwb.ai.controller.model.Concert;
import htwb.ai.controller.model.Ticket;
import htwb.ai.controller.model.TicketTransaction;
import htwb.ai.controller.repo.ConcertsRepository;
import htwb.ai.controller.repo.TicketsRepository;
import htwb.ai.controller.utils.JwtDecode;
import io.jsonwebtoken.*;
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
@RequestMapping(value = "/tickets")
public class TicketsController {

    private final TicketsRepository ticketsRepository;
    private final ConcertsRepository concertsRepository;

    public TicketsController(TicketsRepository ticketsRepository, ConcertsRepository concertsRepository) {
        this.ticketsRepository = ticketsRepository;
        this.concertsRepository = concertsRepository;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<Ticket>> getOwnedTickets(@RequestHeader(value = "Authorization", required = false) String jwt,
                                                        @RequestParam(value = "buyConcert", required = false) Long concertId) {
        Claims claims;
        try {
            claims = JwtDecode.decodeJWT(jwt);
        } catch (ExpiredJwtException | UnsupportedJwtException |
                MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        //Get owner Tickets
        if (concertId == null) {
            List<Ticket> ticketList = ticketsRepository.getTicketsByOwner(claims.getId());
            if (ticketList != null) {
                return new ResponseEntity<>(ticketList, HttpStatus.OK);
            } else
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            //Buy ticket to concert
        } else {
            Concert concert;
            try {
                concert = concertsRepository.findById(concertId).get();
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            //Check if tickets available
            //TODO: Should avoid user getting more than one ticket?
            if (concert.getTickets().size() < concert.getMaxTickets()) {
                Ticket ticket = new Ticket(claims.getId(), concert, new TicketTransaction(false));
                ticketsRepository.save(ticket);
                return new ResponseEntity<>(HttpStatus.OK);
            } else
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
