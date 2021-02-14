package htwb.ai.controller.controller;


import htwb.ai.controller.repo.ConcertsRepository;
import htwb.ai.controller.repo.TicketsRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : Enrico de Chadarevian, Marvin Rausch
 * Project name : Marven
 * @version : 1.0
 * @since : 31-10-2020
 **/
@RestController
@RequestMapping(value = "/tickets")
public class TicketsController {

    private final TicketsRepository repo;

    public TicketsController(TicketsRepository repo) {
        this.repo = repo;
    }

}
