package com.flipkart.gjex.core.config.bundle;

import com.codahale.metrics.health.HealthCheck;
import com.flipkart.gjex.core.Bundle;
import com.flipkart.gjex.core.GJEXConfiguration;
import com.flipkart.gjex.core.filter.Filter;
import com.flipkart.gjex.core.service.Service;
import com.flipkart.gjex.core.setup.Bootstrap;
import com.flipkart.gjex.core.setup.Environment;
import com.flipkart.gjex.core.tracing.TracingSampler;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class ConfigServiceBundle<T extends GJEXConfiguration, U extends Map> implements Bundle<T, U> {

    private final char jsonFlattenSeparator = '-';

    @Override
    public void initialize(Bootstrap<?, ?> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new ConfigServiceConfigurationSourceProvider(bootstrap.getObjectMapper()));
        bootstrap.setConfigurationFactoryFactory(new ConfigServiceConfigurationFactoryFactory<>(jsonFlattenSeparator));
    }

    @Override
    public void run(GJEXConfiguration configuration, Map configMap, Environment environment) {

    }

    @Override
    public List<Service> getServices() {
        return Lists.newArrayList();
    }

    @Override
    public List<Filter> getFilters() {
        return Lists.newArrayList();
    }

    @Override
    public List<HealthCheck> getHealthChecks() {
        return Lists.newArrayList();
    }

    @Override
    public List<TracingSampler> getTracingSamplers() {
        return Lists.newArrayList();
    }
}
