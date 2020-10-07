package com.revature.models;

import lombok.Data;

@Data
public class SwaggerUploadResponse {

    private long eta;
    private String resultRef;
    private int swaggerSummaryId;
    
}
