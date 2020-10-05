package com.revature.models;

import java.net.URI;

import com.revature.responsecollector.JMeterResponseCollector;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @EqualsAndHashCode
public class ResultSummary {
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
    
    public ResultSummary(JMeterResponseCollector logger) {
        super();
        responseAvg = logger.getResponseAvg();
        response25Percentile = logger.getResponse25Percentile();
        response50Percentile = logger.getResponse50Percentile();
        response75Percentile = logger.getResponse75Percentile();
        responseMax = logger.getResponseMax();
        failCount = (logger.getNum4XX() + logger.getNum5XX());
        successFailPercentage = logger.getsuccessFailPercentage();
        reqPerSec = logger.getReqPerSec();
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri2) {
        this.uri = uri2;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getDataReference() {
        return dataReference;
    }

    public void setDataReference(String dataReference) {
        this.dataReference = dataReference;
    }
    
    

}
