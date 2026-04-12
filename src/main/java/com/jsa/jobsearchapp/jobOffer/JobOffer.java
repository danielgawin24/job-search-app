package com.jsa.jobsearchapp.jobOffer;

import com.jsa.jobsearchapp.location.Location;
import com.jsa.jobsearchapp.skill.Skill;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "offer")
@NoArgsConstructor
@Getter
@Setter
public class JobOffer {

    public JobOffer(String url) {
        this.url = url;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false, name = "date_added")
    private LocalDateTime dateAdded;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String category;

    @ManyToMany
    @JoinTable(
            name = "offer_location",
            joinColumns = @JoinColumn(name = "offer_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    Set<Location> locations;

    @ManyToMany
    @JoinTable(
            name = "offer_skill",
            joinColumns = @JoinColumn(name = "offer_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    Set<Skill> skills;

    @Column(name = "employer_name", nullable = false, length = 100)
    private String employerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "seniority", nullable = false, columnDefinition = "enum('INTERN', 'JUNIOR', 'MID', 'SENIOR', 'LEAD_PRINCIPAL', 'C_LEVEL', 'UNSPECIFIED', 'OTHER')")
    private Seniority seniority;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "salary_type", nullable = false)),
            @AttributeOverride(name = "from", column = @Column(name = "salary_from")),
            @AttributeOverride(name = "to", column = @Column(name = "salary_to")),
            @AttributeOverride(name = "currency", column = @Column(name = "salary_currency")),
            @AttributeOverride(name = "isGross", column = @Column(name = "salary_is_gross"))
    })
    private Salary salary;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, columnDefinition = "enum('B2B', 'FULL_TIME', 'PART_TIME', 'INTERNSHIP', 'FREELANCE', 'UNSPECIFIED')")
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_of_contract", nullable = false, columnDefinition = "ENUM('INTERNSHIP', 'TEMPORARY', 'PERMANENT', 'FREELANCE', 'B2B')")
    private TypeOfContract typeOfContract;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "isHybrid", column = @Column(name = "is_hybrid")),
            @AttributeOverride(name = "isRemote", column = @Column(name = "is_remote")),
            @AttributeOverride(name = "isOnSite", column = @Column(name = "is_on_site"))
    })
    private WorkModes workModes;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
