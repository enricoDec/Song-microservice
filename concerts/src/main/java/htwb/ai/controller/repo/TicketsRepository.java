package htwb.ai.controller.repo;


import htwb.ai.controller.model.Concert;
import htwb.ai.controller.model.Ticket;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author : Enrico de Chadarevian, Marvin Rausch
 * Project name : Marven
 * @version : 1.0
 * @since : 22-12-2020
 **/
@Repository
public interface TicketsRepository extends CrudRepository<Ticket, Long> {
    @Query(value = "SELECT * FROM tickets WHERE owner = ?1", nativeQuery = true)
    List<Ticket> getTicketsByOwner(String owner);
}
