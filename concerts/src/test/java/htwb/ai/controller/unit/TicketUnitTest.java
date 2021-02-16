package htwb.ai.controller.unit;


import htwb.ai.controller.controller.TicketsController;
import htwb.ai.controller.model.Concert;
import htwb.ai.controller.model.Ticket;
import htwb.ai.controller.model.TicketTransaction;
import htwb.ai.controller.repo.ConcertsRepository;
import htwb.ai.controller.repo.TicketsRepository;
import htwb.ai.controller.utils.JwtDecode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Collections;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author : Enrico Gamil Toros
 * Project name : MarvEn
 * @version : 1.0
 * @since : 19.01.21
 **/
@ExtendWith(SystemStubsExtension.class)
public class TicketUnitTest {
    @SystemStub
    private final EnvironmentVariables environment = new EnvironmentVariables("SECRET_KEY_KBE", "test_secret_key");
    private ConcertsRepository concertsRepositoryMock;
    private TicketsRepository ticketsRepositoryMock;
    private MockMvc ticketMvc;

    private Concert concert;

    private Ticket ticket;
    private TicketTransaction ticketTransaction;

    private Ticket ticket2;
    private TicketTransaction ticketTransaction2;

    @BeforeEach
    void setup() {
        //Mock repositories
        concertsRepositoryMock = mock(ConcertsRepository.class);
        ticketsRepositoryMock = mock(TicketsRepository.class);

        ticketMvc = MockMvcBuilders.standaloneSetup(
                new TicketsController(ticketsRepositoryMock, concertsRepositoryMock)).build();


        concert = new Concert("Rome", "Black Eyed Peas", 1);
        concert.setConcertId(1L);

        ticketTransaction = new TicketTransaction(false);
        ticketTransaction.setTransactionId(1L);
        ticket = new Ticket("mmuster", concert, ticketTransaction);
        ticket.setTicketId(1L);

        ticketTransaction = new TicketTransaction(false);
        ticketTransaction.setTransactionId(2L);
        ticket2 = new Ticket("enrico", concert, ticketTransaction2);
        ticket2.setTicketId(2L);
    }

    // ---------------------
    // Get all owned tickets
    // ---------------------

    @Test
    @DisplayName("Get all owned tickets as JSON")
    void getOwnedTicketGoodJson() throws Exception {
        String expectedJson = "[{\"ticketId\":1,\"owner\":\"mmuster\",\"concert\":{\"concertId\":1,\"location\":\"Rome\",\"artist\":\"Black Eyed Peas\",\"maxTickets\":1,\"songList\":[]},\"ticketTransaction\":{\"transactionId\":1,\"payed\":false}}]";
        String user = "mmuser";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            Claims claims = mock(Claims.class);
            when(JwtDecode.decodeJWT(jwt)).thenReturn(claims);
            when(claims.getId()).thenReturn(user);


            when(ticketsRepositoryMock.getTicketsByOwner(user)).thenReturn(Collections.singletonList(ticket));
            ResultActions res = ticketMvc.perform(MockMvcRequestBuilders
                    .get("/tickets/")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isOk());

            Assertions.assertEquals(expectedJson, res.andReturn().getResponse().getContentAsString());
        }
    }

    @Test
    @DisplayName("Get all owned tickets as Xml")
    void getOwnedTicketGoodXml() throws Exception {
        String expectedJson = "<List><item><ticketId>1</ticketId><owner>mmuster</owner><concert><concertId>1</concertId><location>Rome</location><artist>Black Eyed Peas</artist><maxTickets>1</maxTickets><songList/></concert><ticketTransaction><transactionId>1</transactionId><payed>false</payed></ticketTransaction></item></List>";
        String user = "mmuser";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            Claims claims = mock(Claims.class);
            when(JwtDecode.decodeJWT(jwt)).thenReturn(claims);
            when(claims.getId()).thenReturn(user);


            when(ticketsRepositoryMock.getTicketsByOwner(user)).thenReturn(Collections.singletonList(ticket));
            ResultActions res = ticketMvc.perform(MockMvcRequestBuilders
                    .get("/tickets/")
                    .accept(MediaType.APPLICATION_XML)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isOk());

            Assertions.assertEquals(expectedJson, res.andReturn().getResponse().getContentAsString());
        }
    }

    @Test
    @DisplayName("Get all owned tickets empty")
    void getOwnedTicketNoTickets() throws Exception {
        String user = "mmuser";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            Claims claims = mock(Claims.class);
            when(JwtDecode.decodeJWT(jwt)).thenReturn(claims);
            when(claims.getId()).thenReturn(user);

            when(ticketsRepositoryMock.getTicketsByOwner(user)).thenReturn(Collections.emptyList());
            ticketMvc.perform(MockMvcRequestBuilders
                    .get("/tickets/")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("Get all owned tickets null")
    void getOwnedTicketNullTickets() throws Exception {
        String user = "mmuser";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            Claims claims = mock(Claims.class);
            when(JwtDecode.decodeJWT(jwt)).thenReturn(claims);
            when(claims.getId()).thenReturn(user);

            when(ticketsRepositoryMock.getTicketsByOwner(user)).thenReturn(null);
            ticketMvc.perform(MockMvcRequestBuilders
                    .get("/tickets/")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("Get all owned tickets unauthorized")
    void getOwnedTicketUnauthorized() throws Exception {
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.decodeJWT(jwt)).thenThrow(MalformedJwtException.class);

            ticketMvc.perform(MockMvcRequestBuilders
                    .get("/tickets/")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isUnauthorized());
        }
    }


    // -------------------------
    // Get a ticket to a concert
    // -------------------------

    @Test
    @DisplayName("Good Test Get a ticket")
    void getTicketGood() throws Exception {
        String user = "mmuser";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            Claims claims = mock(Claims.class);
            when(JwtDecode.decodeJWT(jwt)).thenReturn(claims);
            when(claims.getId()).thenReturn(user);


            when(concertsRepositoryMock.findById(concert.getConcertId())).thenReturn(java.util.Optional.ofNullable(concert));
            ticketMvc.perform(MockMvcRequestBuilders
                    .get("/tickets?buyConcert=" + concert.getConcertId())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isOk());

            verify(ticketsRepositoryMock, times(1)).save(any());
        }
    }

    @Test
    @DisplayName("Get a ticket unauthorized")
    void getTicketUnauthorized() throws Exception {
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.decodeJWT(jwt)).thenThrow(MalformedJwtException.class);

            ticketMvc.perform(MockMvcRequestBuilders
                    .get("/tickets?buyConcert=" + concert.getConcertId())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("Get a ticket not existing")
    void getTicketNotExisting() throws Exception {
        String user = "mmuser";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            Claims claims = mock(Claims.class);
            when(JwtDecode.decodeJWT(jwt)).thenReturn(claims);
            when(claims.getId()).thenReturn(user);

            when(concertsRepositoryMock.findById(concert.getConcertId())).thenThrow(NoSuchElementException.class);
            ticketMvc.perform(MockMvcRequestBuilders
                    .get("/tickets?buyConcert=9999")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isNotFound());

            verify(ticketsRepositoryMock, never()).save(any());
        }
    }

    @Test
    @DisplayName("Get a ticket not existing id")
    void getTicketINotExistingId() throws Exception {
        String user = "mmuser";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            Claims claims = mock(Claims.class);
            when(JwtDecode.decodeJWT(jwt)).thenReturn(claims);
            when(claims.getId()).thenReturn(user);

            ticketMvc.perform(MockMvcRequestBuilders
                    .get("/tickets?buyConcert=-100")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isBadRequest());

            verify(ticketsRepositoryMock, never()).save(any());
        }
    }

    @Test
    @DisplayName("Get a ticket invalid id")
    void getTicketInvalidId() throws Exception {
        String user = "mmuser";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            Claims claims = mock(Claims.class);
            when(JwtDecode.decodeJWT(jwt)).thenReturn(claims);
            when(claims.getId()).thenReturn(user);

            ticketMvc.perform(MockMvcRequestBuilders
                    .get("/tickets?buyConcert=wasd")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isBadRequest());

            verify(ticketsRepositoryMock, never()).save(any());
        }
    }

    @Test
    @DisplayName("Get a ticket max already sold")
    void getTicketMaxTicketCheck() throws Exception {
        String user = "mmuser";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            Claims claims = mock(Claims.class);
            when(JwtDecode.decodeJWT(jwt)).thenReturn(claims);
            when(claims.getId()).thenReturn(user);

            when(concertsRepositoryMock.findById(concert.getConcertId())).thenReturn(java.util.Optional.ofNullable(concert));
            ticketMvc.perform(MockMvcRequestBuilders
                    .get("/tickets?buyConcert=" + concert.getConcertId())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isOk());

            verify(ticketsRepositoryMock, times(1)).save(any());
            concert.setTickets(Collections.singletonList(new Ticket(user, concert, new TicketTransaction(false))));

            ticketMvc.perform(MockMvcRequestBuilders
                    .get("/tickets?buyConcert=" + concert.getConcertId())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isBadRequest());

        }
    }

    // ----------------------------
    // Delete a ticket to a concert
    // ----------------------------

    @Test
    @DisplayName("Good Test delete a ticket")
    void deleteTicketGood() throws Exception {
        String user = ticket.getOwner();
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            Claims claims = mock(Claims.class);
            when(JwtDecode.decodeJWT(jwt)).thenReturn(claims);
            when(claims.getId()).thenReturn(user);


            when(ticketsRepositoryMock.findById(ticket.getTicketId())).thenReturn(java.util.Optional.ofNullable(ticket));
            ticketMvc.perform(MockMvcRequestBuilders
                    .delete("/tickets/" + ticket.getTicketId())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isNoContent());

            verify(ticketsRepositoryMock, times(1)).deleteById(ticket.getTicketId());
        }
    }

    @Test
    @DisplayName("Delete a ticket unauthorized")
    void deleteTicketUnauthorized() throws Exception {
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            when(JwtDecode.decodeJWT(jwt)).thenThrow(MalformedJwtException.class);


            when(ticketsRepositoryMock.findById(ticket.getTicketId())).thenReturn(java.util.Optional.ofNullable(ticket));
            ticketMvc.perform(MockMvcRequestBuilders
                    .delete("/tickets/" + ticket.getTicketId())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isUnauthorized());
            verify(ticketsRepositoryMock, times(0)).deleteById(ticket.getTicketId());
        }
    }

    @Test
    @DisplayName("Delete a ticket invalid Id")
    void deleteTicketInvalidId() throws Exception {
        String user = ticket.getOwner();
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            Claims claims = mock(Claims.class);
            when(JwtDecode.decodeJWT(jwt)).thenReturn(claims);
            when(claims.getId()).thenReturn(user);

            when(ticketsRepositoryMock.findById(ticket.getTicketId())).thenReturn(java.util.Optional.ofNullable(ticket));
            ticketMvc.perform(MockMvcRequestBuilders
                    .delete("/tickets/-1")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isBadRequest());

            verify(ticketsRepositoryMock, times(0)).deleteById(ticket.getTicketId());
        }
    }

    @Test
    @DisplayName("Delete a ticket not existing")
    void deleteTicketNotExistingId() throws Exception {
        String user = ticket.getOwner();
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            Claims claims = mock(Claims.class);
            when(JwtDecode.decodeJWT(jwt)).thenReturn(claims);
            when(claims.getId()).thenReturn(user);

            when(ticketsRepositoryMock.findById(ticket.getTicketId())).thenReturn(java.util.Optional.ofNullable(ticket));
            ticketMvc.perform(MockMvcRequestBuilders
                    .delete("/tickets/9999")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isNotFound());

            verify(ticketsRepositoryMock, times(0)).deleteById(ticket.getTicketId());
        }
    }

    @Test
    @DisplayName("Delete a ticket not owner of ticket")
    void deleteTicketNotOwner() throws Exception {
        String user = "NOT_OWNER";
        String jwt = "BLOB";
        try (MockedStatic<JwtDecode> jwtUtilsMockedStatic = mockStatic(JwtDecode.class)) {
            //Mock authorization
            Claims claims = mock(Claims.class);
            when(JwtDecode.decodeJWT(jwt)).thenReturn(claims);
            when(claims.getId()).thenReturn(user);

            when(ticketsRepositoryMock.findById(ticket.getTicketId())).thenReturn(java.util.Optional.ofNullable(ticket));
            ticketMvc.perform(MockMvcRequestBuilders
                    .delete("/tickets/" + ticket.getTicketId())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, jwt))
                    .andExpect(status().isForbidden());

            verify(ticketsRepositoryMock, times(0)).deleteById(ticket.getTicketId());
        }
    }
}
