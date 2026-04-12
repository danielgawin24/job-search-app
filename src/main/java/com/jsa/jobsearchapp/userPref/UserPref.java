package com.jsa.jobsearchapp.userPref;

import com.jsa.jobsearchapp.jobOffer.EmploymentType;
import com.jsa.jobsearchapp.jobOffer.Seniority;
import com.jsa.jobsearchapp.jobOffer.TypeOfContract;
import com.jsa.jobsearchapp.jobOffer.WorkModes;
import com.jsa.jobsearchapp.skill.Skill;
import com.jsa.jobsearchapp.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "user_pref")
@Getter
@Setter
public class UserPref {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;

    @OneToOne(mappedBy = "userPref")
    private User user;

    @Column(length = 60)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('INTERN','TRAINEE','JUNIOR','MID','SENIOR','EXPERT','C_LEVEL')")
    private Seniority seniority;

    @Column(name = "salary_from")
    private Double salaryFrom;

    @Column(name = "salary_to")
    private Double salaryTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", columnDefinition = "ENUM('INTERNSHIP','MANDATE_CONTRACT','EMPLOYMENT_CONTRACT','B2B','SPECIFIC_TASK_CONTRACT')")
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_of_contract", columnDefinition = "ENUM('INTERNSHIP', 'TEMPORARY', 'PERMANENT', 'FREELANCE', 'B2B')")
    private TypeOfContract typeOfContract;

    @Embedded
    private WorkModes workModes;

    @Column(name = "is_no_location_pref")
    private Boolean isNoLocationPref;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_frequency", columnDefinition = "ENUM('DAILY','WEEKLY','MONTHLY')")
    private EmailFrequency emailFrequency;

    @Column(name = "email_start_date")
    private LocalDateTime emailStartDate;

    @ManyToMany
    @JoinTable(
            name = "user_pref_skill",
            joinColumns = @JoinColumn(name = "user_pref_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    Set<Skill> userPrefSkills;

    private String priorityColumn;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
