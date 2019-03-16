package com.flipkart.gjex.core.config.bundle;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.gjex.core.config.BaseConfigurationFactory;
import com.flipkart.gjex.core.config.ConfigurationException;
import com.flipkart.gjex.core.config.ConfigurationParsingException;
import com.flipkart.gjex.core.config.ConfigurationSourceProvider;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class ConfigServiceConfigurationFactory<T, U extends Map> extends BaseConfigurationFactory<T, U> {

    private final char jsonFlattenSeparator;

    public ConfigServiceConfigurationFactory(Class<T> klass,
                                             Validator validator,
                                             ObjectMapper objectMapper, char jsonFlattenSeparator) {
        super(objectMapper.getFactory(), "JSON", klass, validator, objectMapper);
        this.jsonFlattenSeparator = jsonFlattenSeparator;
    }

    @Override
    public Pair<T, U> build(ConfigurationSourceProvider provider, String path) throws IOException, ConfigurationException {
        try (InputStream input = provider.open(requireNonNull(path))) {
            String configServiceJson = IOUtils.toString(input, Charset.defaultCharset()); // returns json present in config service as String
            String unFlattenedJson = new JsonUnflattener(configServiceJson)
                    .withSeparator(jsonFlattenSeparator)
                    .unflatten(); // returns Config service un-flattened json as Map
            InputStream stream = new ByteArrayInputStream(unFlattenedJson.getBytes(StandardCharsets.UTF_8));
            final JsonNode node = objectMapper.readTree(createParser(stream));
            return build(node, path);
        } catch (JsonParseException e) {
            throw ConfigurationParsingException
                    .builder("Malformed Config service JSON")
                    .setCause(e)
                    .setLocation(e.getLocation())
                    .setDetail(e.getMessage())
                    .build(path);
        }
    }

    @Override
    public Pair<T, U> build() throws IOException, ConfigurationException {
        throw new UnsupportedEncodingException("This method is not supported when using " + ConfigServiceBundle.class.getCanonicalName());
    }
}
