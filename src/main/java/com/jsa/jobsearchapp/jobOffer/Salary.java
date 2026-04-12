package com.jsa.jobsearchapp.jobOffer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Salary {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('SPECIFIED','UNSPECIFIED')")
    private SalaryType type;

    @Column(precision = 10, scale = 2)
    private Double from;

    @Column(precision = 10, scale = 2)
    private Double to;

    @Column(length = 3)
    private String currency;

    private Boolean isGross;
}