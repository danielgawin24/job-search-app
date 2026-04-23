package com.jsa.jobsearchapp.jobOffer;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JobOfferCleanupService {
    private final JobOfferRepository jobOfferRepository;

    public JobOfferCleanupService(JobOfferRepository jobOfferRepository) {
        this.jobOfferRepository = jobOfferRepository;
    }

    @Transactional
    public void cleanUpOldOffers() {
        Instant dateLastSeen = Instant.now().minus(Duration.ofHours((1)));
        jobOfferRepository.deleteInBulkByDateLastSeenBefore(dateLastSeen);
    }
}
