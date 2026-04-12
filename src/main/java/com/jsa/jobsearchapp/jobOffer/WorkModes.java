package com.jsa.jobsearchapp.jobOffer;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@Data
@NoArgsConstructor
public class WorkModes {

    private Boolean isRemote;
    private Boolean isHybrid;
    private Boolean isOnSite;

    public WorkModes setExclusiveMode(String mode) {
        this.isRemote = false;
        this.isHybrid = false;
        this.isOnSite = false;

        switch (mode.toLowerCase()) {
            case "remote":
                this.isRemote = true;
                break;
            case "hybrid":
                this.isHybrid = true;
                break;
            case "onsite":
                this.isOnSite = true;
                break;
        }
        return this;
    }
}
