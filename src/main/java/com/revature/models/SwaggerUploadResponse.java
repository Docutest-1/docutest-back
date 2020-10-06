package com.revature.models;


public class SwaggerUploadResponse {

    private long eta;
    private String resultRef;
    private int swaggerSummaryId;
    
    public long getEta() {
        return eta;
    }
    
    public void setEta(long eta) {
        this.eta = eta;
    }
    
    public String getResultRef() {
        return resultRef;
    }
    
    public void setResultRef(String resultRef) {
        this.resultRef = resultRef;
    }
    
    public int getSwaggerSummaryId() {
        return swaggerSummaryId;
    }
    
    public void setSwaggerSummaryId(int swaggerSummaryId) {
        this.swaggerSummaryId = swaggerSummaryId;
    }
    
    
}
