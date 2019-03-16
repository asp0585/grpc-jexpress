package com.flipkart.gjex.core.config.bundle;

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
        return new ConfigServiceConfigurationFactory<>(klass, validator, objectMapper, jsonFlattenSeparator);
    }
}
