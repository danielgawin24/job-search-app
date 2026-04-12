package com.jsa.jobsearchapp.mail;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mail")
public class MailController {

    private final MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail() {
        mailService.sendSimpleMail(
                "daniel.gawin.2003@gmail.com",
                "Wiadomość testowa op.pl",
                "Jak to czytasz to znaczy że kurwa wyszło!")
        ;
        return ResponseEntity.ok().build();
    }
}
