package com.jsa.jobsearchapp.location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jsa.jobsearchapp.jobOffer.JobOffer;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "location")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "alias_name", nullable = false, length = 50)
    private String aliasName;

    @ManyToMany(mappedBy = "locations")
    @JsonIgnore
    Set<JobOffer> jobOffers;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
