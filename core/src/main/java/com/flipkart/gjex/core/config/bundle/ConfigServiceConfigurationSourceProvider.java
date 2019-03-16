package com.flipkart.gjex.core.config.bundle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.gjex.core.config.ConfigurationSourceProvider;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

public class ConfigServiceConfigurationSourceProvider implements ConfigurationSourceProvider {

    private static final String CONFIG_SVC_URI_SCHEME = "config-svc";
    private static final int CONFIG_SVC_API_VERSION = 1;

    private final ObjectMapper jsonObjectMapper;

    public ConfigServiceConfigurationSourceProvider(ObjectMapper jsonObjectMapper) {
        this.jsonObjectMapper = jsonObjectMapper;
    }

    @Override
    public InputStream open(String path) throws IOException {
        URI uri = URI.create(path);
        if (CONFIG_SVC_URI_SCHEME.equals(uri.getScheme())) {
            System.setProperty("sun.net.client.defaultConnectTimeout", "5000");
            System.setProperty("sun.net.client.defaultReadTimeout", "5000");
            URL url;
            try {
                String fullPath = String.format("/v%d/buckets%s", CONFIG_SVC_API_VERSION, uri.getPath());
                URI httpURI = new URI("http", uri.getAuthority(), fullPath, null, null);
                url = httpURI.toURL();
            } catch (URISyntaxException | MalformedURLException e) {
                throw new IllegalStateException("This won't happen");
            }
            JsonNode jsonNode = jsonObjectMapper.readValue(url, JsonNode.class);
            return IOUtils.toInputStream(jsonNode.get("keys").toString(), Charset.defaultCharset());
        } else {
            throw new RuntimeException("Invalid path.");
        }
    }
}
