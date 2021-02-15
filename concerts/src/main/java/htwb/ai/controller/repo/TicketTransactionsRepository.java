package htwb.ai.controller.repo;

import htwb.ai.controller.model.TicketTransaction;
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
public interface TicketTransactionsRepository extends CrudRepository<TicketTransaction, Long> {
    @Query(value = "SELECT * FROM transactions WHERE transaction_id = ?1", nativeQuery = true)
    List<TicketTransaction> getTransactionsById(String id);
}
