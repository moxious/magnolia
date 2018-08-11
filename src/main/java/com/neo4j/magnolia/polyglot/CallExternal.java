package com.neo4j.magnolia.polyglot;

import com.neo4j.magnolia.config.ExternalFnConfig;
import com.neo4j.magnolia.config.MagnoliaConfiguration;
import org.graalvm.polyglot.Value;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;
import org.neo4j.values.AnyValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CallExternal {
    @Context public GraphDatabaseService db;
    @Context public GraphDatabaseAPI api;
    @Context public Log log;

    private void initializeIfNeeded() throws IOException {
        if (MagnoliaConfiguration.getConfig() == null) {
            MagnoliaConfiguration.initialize(api);
        }
    }

    @UserFunction("magnolia.dynamic")
    @Description("RETURN magnolia.dynamic('js', 'function main() { return 3 + 4; } main();') | call dynamic javascript")
    public Object dynamic(final @Name("lang") String lang, final @Name("code") String code, @Name("arguments") Object arguments) throws IOException {
        log.info("Creating anonymous magnolia function from user input");
        initializeIfNeeded();
        ExternalFn fn = new ExternalFn(lang, code);
        return ValueAdapter.convert(fn.invoke(arguments, db, log));
    }

    @UserFunction("magnolia.listFunctions")
    @Description("RETURN magnolia.listFunctions() | show a list of registered dynamic functions")
    public Object listFunctions() throws IOException {
        initializeIfNeeded();
        return MagnoliaConfiguration.getConfig().getFunctions().stream().map(item -> {
            Map<String,String> c = new HashMap<String,String>();
            c.put("name", item.getName());
            c.put("lang", item.getLanguage());
            c.put("file", item.getFile());
            return c;
        }).collect(Collectors.toList());
    }

    @UserFunction("magnolia.fn")
    @Description("RETURN magnolia.fn('scriptname', { map: value }) | call a magnolia script with a given argument set")
    public Object fn(final @Name("functionName") String externalFn, final @Name("arguments") Object arguments) throws IOException {
        try {
            log.info("Inside of fn, looking for " + externalFn);
            initializeIfNeeded();

            // Look up function, instantiate, run, and return its results.
            ExternalFnConfig config = MagnoliaConfiguration.getConfig().getFunctionByName(externalFn);

            if (config == null) {
                throw new RuntimeException("No such dynamic magnolia function by the name " + externalFn);
            }

            log.info("Loading magnolia function by name " + externalFn + " registered under " + config);
            ExternalFn fn = new ExternalFn(config);
            Object adapted = ValueAdapter.convert(fn.invoke(arguments, db, log));

            log.info("Adapted object is " + adapted + " of class " + (adapted != null ? adapted.getClass() : null));
            return adapted;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
