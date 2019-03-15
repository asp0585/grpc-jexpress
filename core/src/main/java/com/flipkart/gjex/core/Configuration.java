package com.flipkart.gjex.core;

import com.flipkart.gjex.core.config.ApiServiceConfig;
import com.flipkart.gjex.core.config.DashboardServiceConfig;
import com.flipkart.gjex.core.config.GrpcConfig;

public class Configuration {

    private GrpcConfig grpc;
    private ApiServiceConfig apiServiceConfig;
    private DashboardServiceConfig dashboardServiceConfig;

    public GrpcConfig getGrpc() {
        return grpc;
    }

    public void setGrpc(GrpcConfig grpc) {
        this.grpc = grpc;
    }

    public ApiServiceConfig getApiServiceConfig() {
        return apiServiceConfig;
    }

    public void setApiServiceConfig(ApiServiceConfig apiServiceConfig) {
        this.apiServiceConfig = apiServiceConfig;
    }

    public DashboardServiceConfig getDashboardServiceConfig() {
        return dashboardServiceConfig;
    }

    public void setDashboardServiceConfig(DashboardServiceConfig dashboardServiceConfig) {
        this.dashboardServiceConfig = dashboardServiceConfig;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "grpc=" + grpc +
                ", apiServiceConfig=" + apiServiceConfig +
                ", dashboardServiceConfig=" + dashboardServiceConfig +
                '}';
    }
}
