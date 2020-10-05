package com.revature.models;

import java.net.URI;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.revature.responsecollector.JMeterResponseCollector;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@EqualsAndHashCode
@Entity
public class ResultSummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "swagger_summary_id")
    @JsonIgnore
    private SwaggerSummary swaggerSummary;
    
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
        super();
    }
    
    public ResultSummary(URI uri, String httpMethod, long responseAvg,
            long response25Percentile, long response50Percentile, long response75Percentile, long responseMax,
            int failCount, double successFailPercentage, double reqPerSec, String dataReference) {
        super();
        this.uri = uri;
        this.httpMethod = httpMethod;
        this.responseAvg = responseAvg;
        this.response25Percentile = response25Percentile;
        this.response50Percentile = response50Percentile;
        this.response75Percentile = response75Percentile;
        this.responseMax = responseMax;
        this.failCount = failCount;
        this.successFailPercentage = successFailPercentage;
        this.reqPerSec = reqPerSec;
        this.dataReference = dataReference;
    }
    
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public long getResponseAvg() {
        return responseAvg;
    }

    public void setResponseAvg(long responseAvg) {
        this.responseAvg = responseAvg;
    }

    public long getResponse25Percentile() {
        return response25Percentile;
    }

    public void setResponse25Percentile(long response25Percentile) {
        this.response25Percentile = response25Percentile;
    }

    public long getResponse50Percentile() {
        return response50Percentile;
    }

    public void setResponse50Percentile(long response50Percentile) {
        this.response50Percentile = response50Percentile;
    }

    public long getResponse75Percentile() {
        return response75Percentile;
    }

    public void setResponse75Percentile(long response75Percentile) {
        this.response75Percentile = response75Percentile;
    }

    public long getResponseMax() {
        return responseMax;
    }

    public void setResponseMax(long responseMax) {
        this.responseMax = responseMax;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public double getSuccessFailPercentage() {
        return successFailPercentage;
    }

    public void setSuccessFailPercentage(double successFailPercentage) {
        this.successFailPercentage = successFailPercentage;
    }

    public double getReqPerSec() {
        return reqPerSec;
    }

    public void setReqPerSec(double reqPerSec) {
        this.reqPerSec = reqPerSec;
    }

    public String getDataReference() {
        return dataReference;
    }

    public void setDataReference(String dataReference) {
        this.dataReference = dataReference;
    }

    public SwaggerSummary getSwaggerSummary() {
        return swaggerSummary;
    }

    public void setSwaggerSummary(SwaggerSummary swaggerSummary) {
        this.swaggerSummary = swaggerSummary;
    }
    
}
