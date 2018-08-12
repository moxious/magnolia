package com.neo4j.magnolia.polyglot;

import com.neo4j.magnolia.config.ExternalFnConfig;
import com.neo4j.magnolia.config.MagnoliaConfiguration;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.stream.Stream;

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

    @Procedure(mode = Mode.READ)
    @Description("CALL magnolia.listFunctions() | show a list of registered dynamic functions")
    public Stream<ExternalFnConfig> listFunctions() throws IOException {
        initializeIfNeeded();
        return MagnoliaConfiguration.getConfig().getFunctions().stream();
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

            ExternalFn fn = new ExternalFn(config);
            return ValueAdapter.convert(fn.invoke(arguments, db, log));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
