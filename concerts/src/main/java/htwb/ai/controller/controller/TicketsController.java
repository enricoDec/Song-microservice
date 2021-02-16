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

    /**
     * Get ticket of authorized user
     * Example:
     * Get ../songWS/rest/tickets
     * <p>
     * Get a new Ticket
     * Example:
     * Get ../songWS/rest/tickets?buyConcert={id}
     *
     * @param jwt       Jwt Token
     * @param concertId id of concert to get ticket
     * @return owner tickets or 200 if ticket added
     */
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
            if (ticketList != null && !ticketList.isEmpty()) {
                return new ResponseEntity<>(ticketList, HttpStatus.OK);
            } else
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            //Buy ticket to concert
        } else {
            if (concertId < 0)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            Concert concert;
            try {
                concert = concertsRepository.findById(concertId).get();
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            //Check if tickets available
            if (concert.getTickets().size() < concert.getMaxTickets()) {
                Ticket ticket = new Ticket(claims.getId(), concert, new TicketTransaction(false));
                ticketsRepository.save(ticket);
                return new ResponseEntity<>(HttpStatus.OK);
            } else
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Delete a specific Ticket
     * Only authorized owner of playlist can delete his own playlists
     * Example:
     * Delete ../songsWS/rest/ticket/{id}
     *
     * @param jwt      Jwt token
     * @param ticketId id of ticket to be deleted
     * @return HTTP NO CONTENT if playlist has been deleted, UNAUTHORIZED if not authorized,
     * FORBIDDEN if trying to delete playlist that belongs to another user or BAD REQUEST
     */
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteTicket(@RequestHeader(value = "Authorization", required = false) String jwt,
                                               @PathVariable(value = "id") Long ticketId) {
        if (ticketId < 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Claims claims;
        try {
            claims = JwtDecode.decodeJWT(jwt);
        } catch (ExpiredJwtException | UnsupportedJwtException |
                MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        //If authenticated user request to delete his ticket
        Ticket requestedTicket;
        try {
            requestedTicket = ticketsRepository.findById(ticketId).get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (claims.getId().equals(requestedTicket.getOwner())) {
            ticketsRepository.deleteById(ticketId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
