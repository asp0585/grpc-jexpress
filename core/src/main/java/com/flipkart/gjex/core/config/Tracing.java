package com.flipkart.gjex.core.config;


import javax.validation.constraints.NotNull;

public class Tracing {

    @NotNull
    private String collectorEndpoint;

    public String getCollectorEndpoint() {
        return collectorEndpoint;
    }

    public void setCollectorEndpoint(String collectorEndpoint) {
        this.collectorEndpoint = collectorEndpoint;
    }
}
