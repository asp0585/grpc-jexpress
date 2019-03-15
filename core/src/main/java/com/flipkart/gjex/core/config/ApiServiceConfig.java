package com.flipkart.gjex.core.config;

public class ApiServiceConfig {

    private int port;
    private int acceptors;
    private int selectors;
    private int workers;
    private int scheduledExecutorThreadPoolSize;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getAcceptors() {
        return acceptors;
    }

    public void setAcceptors(int acceptors) {
        this.acceptors = acceptors;
    }

    public int getSelectors() {
        return selectors;
    }

    public void setSelectors(int selectors) {
        this.selectors = selectors;
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

    public int getScheduledExecutorThreadPoolSize() {
        return scheduledExecutorThreadPoolSize;
    }

    public void setScheduledExecutorThreadPoolSize(int scheduledExecutorThreadPoolSize) {
        this.scheduledExecutorThreadPoolSize = scheduledExecutorThreadPoolSize;
    }
}
