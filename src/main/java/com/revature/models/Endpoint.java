package com.revature.models;

import lombok.Data;

@Data
public class Endpoint implements Comparable<Endpoint> {
    private String baseUrl;
    private String basePath;
    private String path;
    private int port;

    @Override
    public int compareTo(Endpoint o) {
        // compare baseURL -> port -> basePath -> path
        if (this.baseUrl.equals(o.baseUrl)) {
            if (this.port == o.port) {
                if (this.basePath.equals(o.basePath)) {
                    return this.path.compareTo(o.path);
                }
                return this.basePath.compareTo(o.basePath);
            }
            return this.port - o.port;
        }
        return this.baseUrl.compareTo(o.baseUrl);
    }
}
