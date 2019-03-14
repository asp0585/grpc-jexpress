/*
 * Copyright (c) The original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.flipkart.gjex.guice.module;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.flipkart.gjex.Constants;
import com.flipkart.gjex.core.config.FileLocator;
import com.flipkart.gjex.core.config.YamlConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Names;

/**
 * <code>ConfigModule</code> is a Guice module for loading application configuration attributes 
 * 
 * @author regunath.balasubramanian
 */
public class ConfigModule extends AbstractModule {

	/** Global config map*/
	private static final YamlConfiguration GLOBAL_CONFIG = new YamlConfiguration();
	
	/** Yaml configurations loaded by this module*/
    private final YamlConfiguration[] yamlConfigurations;

    /**
     * Constructor to load the GJEX application startup configuration from System property or classpath
     */
    public ConfigModule() {
        try {
            URL configUrl = null;
            String configFile = System.getProperty(Constants.CONFIG_FILE_PROPERTY);
            if (configFile != null) {
                configUrl = new File(configFile).toURI().toURL();
            } else {
                configUrl = this.getClass().getClassLoader().getResource(Constants.CONFIGURATION_YML);
            }
            yamlConfigurations = new YamlConfiguration[] {new YamlConfiguration(configUrl)};
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructor to load configuration information from files that match the specified file name
     * @param configFileName the configuration file name
     */
    public ConfigModule(String configFileName) {
    		List<YamlConfiguration> yamlConfigurationsList = new LinkedList<YamlConfiguration>();
        try {
        		for (File configFile : FileLocator.findFiles(configFileName)) {
	            yamlConfigurationsList.add(new YamlConfiguration(configFile.toURI().toURL()));
        		}
        		yamlConfigurations = yamlConfigurationsList.toArray(new YamlConfiguration[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static YamlConfiguration getGlobalConfig() {
    		return GLOBAL_CONFIG;
    }
    
    /**
     * Performs concrete bindings for interfaces
     *
     * @see AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bindConfigProperties();
        GLOBAL_CONFIG.addAll(this.yamlConfigurations); // add the loaded YamlConfigurationS to the GLOBAL_CONFIG
    }

    /**
     * Binds individual flattened key-value properties in the configuration yml
     * file. So one can directly inject something like this:
     *
     * @Named("Hibernate.hibernate.jdbcDriver") String jdbcDriver OR
     * @Named("Dashboard.service.port") int port
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void bindConfigProperties() {
    		for (YamlConfiguration yamlConfiguration: yamlConfigurations) {
    			// we will not bind the entire YamlConfiguration but instead the individual properties that are injectable
	        // bind(YamlConfiguration.class).toInstance(yamlConfiguration);
	        Iterator<String> propertyKeys = yamlConfiguration.getKeys();
	        while (propertyKeys.hasNext()) {
	            String propertyKey = propertyKeys.next();
	            Object propertyValue = yamlConfiguration.getProperty(propertyKey);
	            LinkedBindingBuilder annotatedWith = bind(propertyValue.getClass()).annotatedWith(Names.named(propertyKey));
	            annotatedWith.toInstance(propertyValue);
	        }
    		}
    }
}
