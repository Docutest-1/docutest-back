package com.revature.services;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class S3ServiceTest {

    private S3Services s3s;

    @BeforeEach
    public void setUp() throws Exception {
        s3s = new S3Services();
    }
    
    @Test
    void testS3() {
        s3s.uploadfile("Test String");

    }
}
