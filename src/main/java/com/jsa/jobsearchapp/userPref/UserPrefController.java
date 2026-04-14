package com.jsa.jobsearchapp.userPref;

import com.jsa.jobsearchapp.request.UserPrefRequest;
import com.jsa.jobsearchapp.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/preferences")
public class UserPrefController {

    private final UserService userService;

    public UserPrefController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/get/{username}")
    public ResponseEntity<UserPrefDTO> getPreferences(@PathVariable String username) {
        return new ResponseEntity<>(userService.getPreferences(username), HttpStatus.OK);
    }

    @PutMapping("/put/{username}")
    public ResponseEntity<UserPrefDTO> replacePreferences(@RequestBody UserPrefRequest request, @PathVariable String username) {
        return new ResponseEntity<>(userService.putPreferences(username, request), HttpStatus.OK);
    }

    @PatchMapping("/patch/{username}")
    public ResponseEntity<UserPrefDTO> patchPreferences(@RequestBody UserPrefRequest request, @PathVariable String username) {
        return new ResponseEntity<>(userService.patchPreferences(username, request), HttpStatus.OK);
    }

    /*
    @PutMapping("/employees/{id}")
  Employee replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) {


  }
     */

//    @PatchMapping("/updateSome/{username}")
//    public ResponseEntity<?> updateSomePreferences(@RequestBody UserPrefRequest request, @PathVariable String username) {
//
//    }

}
