package com.neo4j.googlecloud.pubsub;

import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import java.util.HashMap;
import java.util.Map;

public class PubsubConfiguration {
    public static final String PREFIX = "google.pubsub.";
    private static Map<String, Object> config = new HashMap<>(10);

    public static void initialize(GraphDatabaseAPI db) {
        System.out.println("Initializing Pubsub configuration");
        Map<String, String> params = db.getDependencyResolver().resolveDependency(Config.class).getRaw();
        config.clear();
        config.putAll(subMap(params, PREFIX));
        System.out.println(config);
    }

    public static Map<String, Object> subMap(Map<String, ?> params, String prefix) {
        Map<String, Object> config = new HashMap<>(10);
        int len = prefix.length() + (prefix.isEmpty() || prefix.endsWith(".") ? 0 : 1);
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(prefix)) {
                config.put(key.substring(len), entry.getValue());
            }
        }
        return config;
    }

    public static Map<String, Object> get(String prefix) {
        return subMap(config, prefix);
    }

    public static <T> T get(String key, T defaultValue) {
        return (T) config.getOrDefault(key, defaultValue);
    }

    public static boolean isEnabled(String key) {
        return (Boolean)config.getOrDefault(key, new Boolean(false));
    }

    public static Map<String,Object> getConfig() {
        return config;
    }
}
