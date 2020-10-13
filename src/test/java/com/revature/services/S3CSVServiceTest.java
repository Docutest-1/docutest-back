package com.revature.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;

class S3CSVServiceTest {

    private S3CSVService s3Service;
    private AmazonS3 mockClient;
    
    @BeforeEach
    public void setup() {
        mockClient = mock(AmazonS3.class);
        s3Service = new S3CSVService(mockClient);
    }
    
    @Test
    public void testGetObjectFromBucket() {
        S3Object expected = new S3Object();
        when(mockClient.getObject(Matchers.eq("docutestbucket"), Matchers.eq("test"))).thenReturn(expected);
        
        S3Object actual = s3Service.getObjectFromBucket("test");
        assertEquals(expected, actual);
    }
    
    @Test
    public void testGetObjectFromBucket_doesNotExist() {
        when(mockClient.getObject(Matchers.eq("docutestbucket"), Matchers.eq("test"))).thenThrow(SdkClientException.class);
        
        S3Object actual = s3Service.getObjectFromBucket("test");
        assertEquals(null, actual);
    }
    
    @Test
    public void testPutObjectInBucket() {
        InputStream file = mock(InputStream.class);
        assertTrue(s3Service.putObjectInBucket("test", file));
    }
    
    @Test
    public void testPutObjectInBucket_exception() {
        InputStream file = mock(InputStream.class);
        
        when(mockClient.putObject(Matchers.any())).thenThrow(SdkClientException.class);
        
        assertFalse(s3Service.putObjectInBucket("test", file));
    }
    
}
