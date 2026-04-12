package com.jsa.jobsearchapp.jobOffer;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/offer")
public class JobOfferController {

    private final JobOfferRepository jobOfferRepository;

    public JobOfferController(JobOfferRepository jobOfferRepository) {
        this.jobOfferRepository = jobOfferRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobOfferDTO> getJobOffer(@PathVariable int id) {
        JobOfferDTO dto = new JobOfferDTO(jobOfferRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Offer not found for id: " + id))
        );
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
