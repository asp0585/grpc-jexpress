package com.flipkart.gjex.core.config;

import com.flipkart.gjex.core.GJEXConfiguration;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.flattener.PrintMode;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface ConfigurationFactory<T extends GJEXConfiguration, U extends Map> {

    /**
     * Loads, parses, binds, and validates a configuration object.
     *
     * @param provider the provider to to use for reading configuration files
     * @param path     the path of the configuration file
     * @return A pair of validated configuration object (T) and flattened json config as map (U)
     * @throws IOException            if there is an error reading the file
     * @throws ConfigurationException if there is an error parsing or validating the file
     */
    Pair<T, U> build(ConfigurationSourceProvider provider, String path) throws IOException, ConfigurationException;

    /**
     * Loads, parses, binds, and validates a configuration object from a file.
     *
     * @param file the path of the configuration file
     * @return A pair of validated configuration object (T) and flattened json config as map (U)
     * @throws IOException            if there is an error reading the file
     * @throws ConfigurationException if there is an error parsing or validating the file
     */
    default Pair<T, U> build(File file) throws IOException, ConfigurationException {
        return build(new FileConfigurationSourceProvider(), file.toString());
    }

    /**
     * Loads, parses, binds, and validates a configuration object from an empty document.
     *
     * @return A pair of validated configuration object (T) and flattened json config as map (U)
     * @throws IOException            if there is an error reading the file
     * @throws ConfigurationException if there is an error parsing or validating the file
     */
    Pair<T, U> build() throws IOException, ConfigurationException;

    /**
     * This function returns a flattened json for for provided json as string
     *
     * @param json string
     * @return flattened json as string
     * This is used to generate flattened json from given config file
     */
    default String getFlattenedJson(String json) {
        return new JsonFlattener(json)
                .withSeparator('.')
                .withPrintMode(PrintMode.PRETTY)
                .flatten();
    }

}
