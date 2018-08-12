package com.neo4j.magnolia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MagnoliaConfiguration {
    private static MagnoliaConfiguration config = null;
    private static final String PREFIX = "magnolia";
    private static DirectoryWatcher watcher = null;
    private static Thread watcherThread = null;

    public static synchronized MagnoliaConfiguration initialize(String filename) throws IOException {
        if (filename == null) {
            System.err.println("No filename provided; Magnolia cannot load configuration");
            return null;
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        MagnoliaConfiguration conf = mapper.readValue(new File(filename), MagnoliaConfiguration.class);
        System.out.println("Loaded Magnolia Configuration from " + filename + ": " + conf);
        config = conf;

        // If there was a previous watcher from an earlier configuration, kill it.
        if (watcherThread != null) {
            watcherThread.interrupt();
        }

        // Config may have changed base path.  Set a new watcher on that base path
        watcher = new DirectoryWatcher(config.getBasePath());
        watcherThread = new Thread(watcher);

        watcherThread.start();
        return config;
    }

    /**
     * Returns the filename specified in neo4j.conf under magnolia.configuration, this is where YAML
     * configuration is expected.
     * @param db
     * @return String filename from the config file.
     */
    public static String getConfigurationFilePath(GraphDatabaseAPI db) {
        Map<String, String> params = db.getDependencyResolver().resolveDependency(Config.class).getRaw();

        Map<String,Object> magnolia = subMap(params, PREFIX);
        String filename = magnolia.get("configuration").toString();
        return filename;
    }

    public static synchronized MagnoliaConfiguration initialize(GraphDatabaseAPI db) throws IOException {
        return initialize(getConfigurationFilePath(db));
    }

    private String basePath = "/plugins/magnolia";
    private List<ExternalFnConfig> functions;

    public List<ExternalFnConfig> getFunctions() {
        return functions;
    }

    public void setFunctions(List<ExternalFnConfig> functions) {
        this.functions = functions;
    }

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
        return "Magnolia Configuration:\n\n" +
                "basePath: " + this.basePath + "\n" +
                "Functions: " + this.functions.stream().map(ExternalFnConfig::toString).collect(Collectors.joining(""));
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
