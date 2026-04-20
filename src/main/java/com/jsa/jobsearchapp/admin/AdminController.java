package com.jsa.jobsearchapp.admin;

import com.jsa.jobsearchapp.jobOffer.JobOffer;
import com.jsa.jobsearchapp.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final JustJoinItService justJoinItService;
    private final UserRepository userRepository;

    public AdminController(AdminService adminService, JustJoinItService justJoinItService, UserRepository userRepository) {
        this.adminService = adminService;
        this.justJoinItService = justJoinItService;
        this.userRepository = userRepository;
    }

    @GetMapping("/force-import")
    public ResponseEntity<String> forceImport() {
        return new ResponseEntity<>(adminService.forceImportOffers(), HttpStatus.OK);
    }

    @GetMapping("/force-send")
    public ResponseEntity<String> forceSend() {
        return new ResponseEntity<>(adminService.forceSendOffers(), HttpStatus.OK);
    }

    @GetMapping("/test-justjoinit")
    public ResponseEntity<String> testJustJoinIt() {
        CompletableFuture<List<JobOffer>> urls = justJoinItService.getJobOffers();
        return new ResponseEntity<>("Just joined " + urls.join().size() + " jobs in the db", HttpStatus.OK);
    }
}
