package com.jsa.jobsearchapp.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jsa.jobsearchapp.jobOffer.EmploymentType;
import com.jsa.jobsearchapp.jobOffer.Seniority;
import com.jsa.jobsearchapp.jobOffer.TypeOfContract;
import com.jsa.jobsearchapp.jobOffer.WorkModes;
import com.jsa.jobsearchapp.userPref.EmailFrequency;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserPrefRequest {

    private String country;
    private String city;
    private Seniority seniority;
    private String salaryFrom;
    private String salaryTo;
    private EmploymentType employmentType;
    private TypeOfContract typeOfContract;
    private WorkModes workModes;
    private List<String> skills;
    private String priorityColumn;
    private Boolean isNoLocationPref;
    private EmailFrequency emailFrequency;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime emailStartDate;
}
