package com.revature.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @EqualsAndHashCode
public class ResultSummary {
    private String uri = "";
    private String httpMethod;
    private int responseAvg;
    private int response25Percentile;
    private int response50Percentile;
    private int response75Percentile;
    private int responseMax;
    private int failCount;
    private int successFailPercentage;
    private double reqPerSec;
    private String dataReference;
}
