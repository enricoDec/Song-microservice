package htwb.ai.controller.repo;


import htwb.ai.controller.model.Concert;
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
public interface ConcertsRepository extends CrudRepository<Concert, Long> {
    @Query(value = "SELECT * FROM concerts", nativeQuery = true)
    List<Concert> getAll();
}
