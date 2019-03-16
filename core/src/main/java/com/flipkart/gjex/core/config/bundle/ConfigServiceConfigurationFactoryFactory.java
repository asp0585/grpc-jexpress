package com.flipkart.gjex.core.config.bundle;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.gjex.core.config.ConfigurationFactory;
import com.flipkart.gjex.core.config.ConfigurationFactoryFactory;

import javax.validation.Validator;
import java.util.Map;

public class ConfigServiceConfigurationFactoryFactory<T, U extends Map> implements ConfigurationFactoryFactory<T, U> {

    private final char jsonFlattenSeparator;

    public ConfigServiceConfigurationFactoryFactory(char jsonFlattenSeparator) {
        this.jsonFlattenSeparator = jsonFlattenSeparator;
    }

    @Override
    public ConfigurationFactory<T, U> create(Class<T> klass, Validator validator, ObjectMapper objectMapper) {
        return new ConfigServiceConfigurationFactory<>(klass, validator, configureObjectMapper(objectMapper.copy()), jsonFlattenSeparator);
    }

    /**
     * Provides additional configuration for the {@link ObjectMapper} used to read
     * the configuration. By default {@link DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES}
     * is enabled to protect against misconfiguration.
     *
     * @param objectMapper template to be configured
     * @return configured object objectMapper
     */
    protected ObjectMapper configureObjectMapper(ObjectMapper objectMapper) {
        return objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
