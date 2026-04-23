package com.jsa.jobsearchapp.userPref;

import com.jsa.jobsearchapp.jobOffer.EmploymentType;
import com.jsa.jobsearchapp.jobOffer.Seniority;
import com.jsa.jobsearchapp.jobOffer.TypeOfContract;
import com.jsa.jobsearchapp.jobOffer.WorkModes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrefDTO {

    private String city;
    private Seniority seniority;
    private String salaryFrom;
    private String salaryTo;
    private EmploymentType employmentType;
    private TypeOfContract typeOfContract;
    private WorkModes workModes;
    private List<String> skills;
    private PriorityColumn priorityColumn;
    private Boolean isNoLocationPref;

}
