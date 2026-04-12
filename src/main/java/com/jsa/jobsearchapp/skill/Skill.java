package com.jsa.jobsearchapp.skill;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jsa.jobsearchapp.jobOffer.JobOffer;
import com.jsa.jobsearchapp.userPref.UserPref;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "skill")
@Data
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "alias_name", nullable = false, length = 50)
    private String aliasName;

    @ManyToMany(mappedBy = "skills")
    @JsonIgnore
    Set<JobOffer> jobOffers;

    @ManyToMany(mappedBy = "userPrefSkills")
    @JsonIgnore
    Set<UserPref> userPrefs;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
