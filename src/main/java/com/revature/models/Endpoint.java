package com.revature.models;

import lombok.Data;

@Data
public class Endpoint {
    private String baseUrl;
    private String basePath;
    private String path;
    private int port;
}
