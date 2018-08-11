package com.neo4j.magnolia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MagnoliaConfiguration {
    private static MagnoliaConfiguration config = null;
    private static final String PREFIX = "magnolia";

    public static MagnoliaConfiguration initialize(String filename) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        MagnoliaConfiguration conf = mapper.readValue(new File(filename), MagnoliaConfiguration.class);
        System.out.println("Loaded Magnolia Configuration from " + filename + ": " + conf);
        config = conf;

        DirectoryWatcher watcher = new DirectoryWatcher(config.getBasePath());
        new Thread(watcher).start();

        return config;
    }

    public static MagnoliaConfiguration initialize(GraphDatabaseAPI db) throws IOException {
        System.out.println("Initializing Magnolia configuration");
        Map<String, String> params = db.getDependencyResolver().resolveDependency(Config.class).getRaw();

        Map<String,Object> magnolia = subMap(params, PREFIX);
        String filename = magnolia.get("configuration").toString();
        return initialize(filename);
    }

    public List<ExternalFnConfig> getFunctions() {
        return functions;
    }

    public void setFunctions(List<ExternalFnConfig> functions) {
        this.functions = functions;
    }

    private String basePath = "/plugins/magnolia";
    private List<ExternalFnConfig> functions;

    /**
     * Get a function by its name
     * @param name function name, as configured in magnolia.yaml
     * @return the configuration for that function, or null if none exists.
     */
    public ExternalFnConfig getFunctionByName(String name) {
        Stream<ExternalFnConfig> externalFnConfigStream = functions.stream()
                .filter(fnConfig -> name.equals(fnConfig.getName()));

        try {
            return externalFnConfigStream.findFirst().get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public String toString() {
        return "Magnolia Configuration:\n\nFunctions: " + this.functions.stream().map(ExternalFnConfig::toString).collect(Collectors.joining("\n"));
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

    public static MagnoliaConfiguration getConfig() {
        return config;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
