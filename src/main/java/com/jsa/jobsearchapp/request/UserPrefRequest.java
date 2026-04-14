package com.jsa.jobsearchapp.request;

import com.jsa.jobsearchapp.jobOffer.EmploymentType;
import com.jsa.jobsearchapp.jobOffer.Seniority;
import com.jsa.jobsearchapp.jobOffer.TypeOfContract;
import com.jsa.jobsearchapp.jobOffer.WorkModes;
import com.jsa.jobsearchapp.userPref.PriorityColumn;
import lombok.Data;

import java.util.List;

@Data
public class UserPrefRequest {

    private String city;
    private Seniority seniority;
    private Double salaryFrom;
    private Double salaryTo;
    private EmploymentType employmentType;
    private TypeOfContract typeOfContract;
    private WorkModes workModes;
    private List<String> skills;
    private PriorityColumn priorityColumn;
    private Boolean isNoLocationPref;

    public boolean isAnyFieldNull() {
        return this.city == null
                || this.seniority == null
                || this.salaryFrom == null
                || this.salaryTo == null
                || this.employmentType == null
                || this.typeOfContract == null
                || this.workModes == null
                || this.skills == null
                || this.priorityColumn == null
                || this.isNoLocationPref == null;
    }
}
