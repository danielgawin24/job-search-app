package com.jsa.jobsearchapp.jobOffer;

import com.jsa.jobsearchapp.location.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobOfferDTO {

    private String url;
    private String employerName;
    private Set<String> locations;
    private Seniority seniority;
    private Salary salary;
    private EmploymentType employmentType;
    private TypeOfContract typeOfContract;
    private WorkModes workModes;
    private Instant dateLastSeen;

    public JobOfferDTO(JobOffer jobOffer) {
        this.url = jobOffer.getUrl();
        this.employerName = jobOffer.getEmployerName();
        Set<Location> locationCopy = new HashSet<>(jobOffer.getLocations());
        this.locations = locationCopy.stream()
                .map(Location::getDisplayName)
                .collect(Collectors.toSet());
        this.seniority = jobOffer.getSeniority();
        this.salary = jobOffer.getSalary();
        this.employmentType = jobOffer.getEmploymentType();
        this.typeOfContract = jobOffer.getTypeOfContract();
        this.workModes = jobOffer.getWorkModes();
        this.dateLastSeen = jobOffer.getDateLastSeen();
    }
}
