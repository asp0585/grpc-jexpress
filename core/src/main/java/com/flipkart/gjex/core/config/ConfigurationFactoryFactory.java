package com.flipkart.gjex.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.Validator;

public interface ConfigurationFactoryFactory<T> {

    ConfigurationFactory<T> create(Class<T> klass,
                                   Validator validator,
                                   ObjectMapper objectMapper,
                                   String propertyPrefix);
}
