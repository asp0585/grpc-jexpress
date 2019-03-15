package com.flipkart.gjex.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.gjex.core.config.ApiService;
import com.flipkart.gjex.core.config.DashboardService;
import com.flipkart.gjex.core.config.GrpcConfig;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Configuration {

    @Valid
    @NotNull
    @JsonProperty("Grpc")
    private GrpcConfig grpc;

    @Valid
    @NotNull
    @JsonProperty("Api")
    private ApiService apiService;

    @Valid
    @NotNull
    @JsonProperty("Dashboard")
    private DashboardService dashboardService;

    public GrpcConfig getGrpc() {
        return grpc;
    }

    public void setGrpc(GrpcConfig grpc) {
        this.grpc = grpc;
    }

    public ApiService getApiService() {
        return apiService;
    }

    public void setApiService(ApiService apiService) {
        this.apiService = apiService;
    }

    public DashboardService getDashboardService() {
        return dashboardService;
    }

    public void setDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "grpc=" + grpc +
                ", apiService=" + apiService +
                ", dashboardService=" + dashboardService +
                '}';
    }
}
