package com.revature.models;

import java.net.URI;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.revature.responsecollector.JMeterResponseCollector;
import lombok.Data;

@Data
@Entity
public class ResultSummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private URI uri;
    private String httpMethod;
    private long responseAvg;
    private long response25Percentile;
    private long response50Percentile;
    private long response75Percentile;
    private long responseMax;
    private int failCount;
    private double successFailPercentage;
    private double reqPerSec;
    private String dataReference;
        
    public ResultSummary() {
    }
    
    public ResultSummary(URI uri, String httpMethod, JMeterResponseCollector logger) {
        super();
        this.uri = uri;
        this.httpMethod = httpMethod;
        responseAvg = logger.getResponseAvg();
        response25Percentile = logger.getResponse25Percentile();
        response50Percentile = logger.getResponse50Percentile();
        response75Percentile = logger.getResponse75Percentile();
        responseMax = logger.getResponseMax();
        failCount = (logger.getNum4XX() + logger.getNum5XX());
        successFailPercentage = logger.getsuccessFailPercentage();
        reqPerSec = logger.getReqPerSec();
    }
        
}
