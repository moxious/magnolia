package com.neo4j.magnolia.polyglot;

import com.neo4j.magnolia.config.ExternalFnConfig;
import com.neo4j.magnolia.config.MagnoliaConfiguration;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.io.IOException;

public class CallExternal {
    @Context public GraphDatabaseService db;
    @Context public Log log;

    @UserFunction("magnolia.dynamic")
    @Description("RETURN magnolia.dynamic('scriptname', { map: value }) | call a magnolia script with a given argument set")
    public Object dynamic(final @Name("functionName") String externalFn, final @Name("arguments") Object arguments) throws IOException {
        MagnoliaConfiguration.initialize();

        // Look up function, instantiate, run, and return its results.
        ExternalFnConfig config = MagnoliaConfiguration.config.getFunctionByName(externalFn);
        ExternalFn fn = new ExternalFn(config);
        return fn.invoke(arguments, db, log);
    }
}
