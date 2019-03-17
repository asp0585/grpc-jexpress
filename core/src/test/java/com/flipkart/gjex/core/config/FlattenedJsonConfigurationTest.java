package com.flipkart.gjex.core.config;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

public class FlattenedJsonConfigurationTest {

    private FlattenedJsonConfiguration configuration;

    @Before
    public void setUp() throws Exception {
        Map<String, Object> configMap = Maps.newHashMap();
        configMap.put("Grpc.port", 50051);
        configMap.put("Dashboard.port", 9999);
        configMap.put("Dashboard.acceptors", 5);
        configMap.put("Dashboard.workers", 5);
        configuration = new FlattenedJsonConfiguration(configMap);
    }

    @Test
    public void getKeySuccess() {
        assertEquals(50051, configuration.getInt("Grpc.port"));
        assertEquals(9999, configuration.getInt("Dashboard.port"));
    }

    @Test(expected = NoSuchElementException.class)
    public void getKeyFailure() {
        assertEquals(50051, configuration.getInt("Grpc.key.does.not.exist"));
    }



}