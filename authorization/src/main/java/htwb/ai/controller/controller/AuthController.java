package htwb.ai.controller.controller;


import htwb.ai.controller.model.User;
import htwb.ai.controller.repo.UserRepository;
import htwb.ai.controller.utils.JwtCompile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.NoResultException;
import javax.validation.Valid;

/**
 * @author : Enrico de Chadarevian, Marvin Rausch
 * Project name : Marven
 * @version : 1.0
 * @since : 31-10-2020
 **/
@RestController
@RequestMapping(value = "/auth")
public class AuthController {

    private final UserRepository repo;

    public AuthController(UserRepository repo) {
        this.repo = repo;
    }

    /**
     * POST User credentials authenticate and return a token
     * <p>
     * POST http://localhost:8080/rest/auth
     * ACCEPTS ONLY JSON
     *
     * @param user User
     * @return Response
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> auth(@RequestBody @Valid User user) {
        User comparisonUser;

        try {
            //Check if User exists
            comparisonUser = repo.findUserByUserId(user.getUserId());
        } catch (NoResultException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // If user wasn't found and for some reason no exception was thrown
        if (comparisonUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        //Wrong Password
        if (!comparisonUser.getPassword().equals(user.getPassword()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        //Create Token and add token to header
        HttpHeaders header = new HttpHeaders();

        return ResponseEntity.ok().headers(header).body(JwtCompile.createJWT(comparisonUser.getUserId(),
                comparisonUser.getLastName()));
    }

}
