package htwb.ai.controller.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import htwb.ai.controller.model.Concert;
import htwb.ai.controller.model.Ticket;
import htwb.ai.controller.model.TicketTransaction;
import htwb.ai.controller.repo.ConcertsRepository;
import htwb.ai.controller.repo.TicketsRepository;
import htwb.ai.controller.utils.JwtDecode;
import htwb.ai.controller.utils.QRUtils;
import io.jsonwebtoken.*;
import org.apache.commons.io.FileExistsException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
    private final QRUtils qrUtils;

    public TicketsController(TicketsRepository ticketsRepository,
                             ConcertsRepository concertsRepository,
                             QRUtils qrUtils) {
        this.ticketsRepository = ticketsRepository;
        this.concertsRepository = concertsRepository;
        this.qrUtils = qrUtils;
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
     * @return owner tickets and 200 or 201 if ticket created
     * @throws URISyntaxException URISyntaxException
     */
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<Ticket>> getOwnedTickets(@RequestHeader(value = "Authorization", required = false) String jwt,
                                                        @RequestParam(value = "buyConcert", required = false) Long concertId) throws URISyntaxException {
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
                //Need to save ticket to get id and qrCodePath can't be null
                ticketsRepository.save(ticket);
                try {
                    //Make qrCode from Ticket and set path in ticket
                    ticket.setQrCodePath(qrUtils.generateTicketQR(ticket));
                } catch (JsonProcessingException | FileExistsException e) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }

                //Location Header
                Long newId = ticketsRepository.save(ticket).getTicketId();
                HttpHeaders responseHeaders = new HttpHeaders();
                URI locationURI = new URI(String.format("/rest/tickets/%d", newId));
                return ResponseEntity.created(locationURI).headers(responseHeaders).body(null);
            } else
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get qrCode for ticket
     * Example:
     * Accept: image/png
     * Get ../songWS/rest/tickets/{id}
     *
     * @param jwt      Jwt Token
     * @param ticketId id of ticket to get ticket
     * @return owner tickets or 200 if ticket added
     */
    @GetMapping(value = "/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getTicketQR(@RequestHeader(value = "Authorization", required = false) String jwt,
                                              @PathVariable(value = "id") Long ticketId) {
        Claims claims;
        try {
            claims = JwtDecode.decodeJWT(jwt);
        } catch (ExpiredJwtException | UnsupportedJwtException |
                MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (ticketId < 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //Check if Ticket exists
        Ticket requestedTicket;
        try {
            requestedTicket = ticketsRepository.findById(ticketId).get();
            //Check if the requested ticket is owned by auth user
            if (!claims.getId().equals(requestedTicket.getOwner()))
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Get Qr code and return
        String path = requestedTicket.getQrCodePath();
        File file = new File(path);
        byte[] qrCode;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            qrCode = fileInputStream.readAllBytes();
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(qrCode, HttpStatus.OK);
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
            qrUtils.deleteTicketQR(requestedTicket.getQrCodePath());
            ticketsRepository.deleteById(ticketId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
