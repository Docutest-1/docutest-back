package com.revature.models;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Entity
public class SwaggerSummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ResultSummary> resultsummaries = new HashSet<>();
    
    private long eta;
    
    public SwaggerSummary() {
        super();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<ResultSummary> getResultsummaries() {
        return resultsummaries;
    }

    public void setResultsummaries(Set<ResultSummary> resultsummaries) {
        this.resultsummaries = resultsummaries;
    }

    public long getEta() {
        return eta;
    }

    public void setEta(long eta) {
        this.eta = eta;
    }
}
