package htwb.ai.MarvEn.controller;


import htwb.ai.MarvEn.utils.JwtUtils;
import htwb.ai.MarvEn.model.User;
import htwb.ai.MarvEn.repo.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.NoResultException;

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
    public ResponseEntity<String> auth(@RequestBody User user) {
        User comparisonUser;
        if (user.getUserId() == null || user.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            //Check if User exists
            comparisonUser = repo.findUserByUserId(user.getUserId());
        } catch (NoResultException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // If user wasn't found and for some reason no exception was thrown
        if (comparisonUser == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        //Wrong Password
        if (!comparisonUser.getPassword().equals(user.getPassword()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        //Create Token and add token to header
        HttpHeaders header = new HttpHeaders();
        //Last name is not required for auth, so we get it by id

        return ResponseEntity.ok().headers(header).body(JwtUtils.createJWT(user.getUserId(),
                repo.findUserByUserId(user.getUserId()).getLastName()));
    }

}
