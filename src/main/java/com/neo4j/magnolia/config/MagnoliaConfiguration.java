package com.neo4j.magnolia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class MagnoliaConfiguration {
    public static MagnoliaConfiguration config = null;
    public static MagnoliaConfiguration initialize() throws IOException {
        if (config != null) return config;

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        MagnoliaConfiguration conf = mapper.readValue(new File("/Users/davidallen/hax/magnolia/external/magnolia.yaml"), MagnoliaConfiguration.class);
        System.out.println(conf);
        config = conf;
        return config;
    }

    public List<ExternalFnConfig> getFunctions() {
        return functions;
    }

    public void setFunctions(List<ExternalFnConfig> functions) {
        this.functions = functions;
    }

    private List<ExternalFnConfig> functions;

    public ExternalFnConfig getFunctionByName(String name) {
        return functions.stream()
                .filter(fnConfig -> name.equals(fnConfig.getName()))
                .findFirst()
                .get();
    }

    public String toString() {
        return "Magnolia Configuration:\n\nFunctions: " + this.functions.stream().map(ExternalFnConfig::toString).collect(Collectors.joining("\n\n"));
    }

    public static void main(String [] args) throws Exception {
        initialize();
    }
}
