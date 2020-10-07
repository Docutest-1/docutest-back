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

import lombok.Data;

@Data
@Entity
public class SwaggerSummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String testPlanName;
    private int loops;
    private int duration;
    private int threads;
    private int rampUp;
    private boolean followRedirects;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ResultSummary> resultsummaries = new HashSet<>();
    
}
