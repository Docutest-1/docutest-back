package com.revature.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Component;

@Component
public class S3CSVService {
    
    private AmazonS3 s3Client;
    private static final String BUCKET_NAME = "docutestbucket";
    
    public S3CSVService() {
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(Regions.US_EAST_2)
                .build();
    }
    
    public S3CSVService(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }
        
    public S3Object getObjectFromBucket(String key) {
        S3Object object;
        try {
            object = s3Client.getObject(BUCKET_NAME, key);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        
        return object;
    }
    
    public boolean putObjectInBucket(String key, InputStream file) {
        try {
            s3Client.putObject(new PutObjectRequest(BUCKET_NAME, key, file, new ObjectMetadata())
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (Exception ex) {
            return false;
        }
        
        return true;
    }
    
    public CSVWriter createWriter(ByteArrayOutputStream stream) {
        OutputStreamWriter streamWriter = new OutputStreamWriter(stream);
        
        return new CSVWriter(streamWriter);
    }

}
