package com.jsa.jobsearchapp.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity<String> deleteAll() {
        return new ResponseEntity<>(userService.deleteAllUsersExceptAdmin(), HttpStatus.OK);
    }

}
