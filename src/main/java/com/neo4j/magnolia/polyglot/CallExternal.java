package com.neo4j.magnolia.polyglot;

import com.neo4j.magnolia.config.ExternalFnConfig;
import com.neo4j.magnolia.config.MagnoliaConfiguration;
import org.graalvm.polyglot.Value;
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


    @Procedure(mode = Mode.READ, name="magnolia.list")
    @Description("CALL magnolia.list() | show a list of registered dynamic functions")
    public Stream<ExternalFnConfig> list() throws IOException {
        initializeIfNeeded();

        Stream <ExternalFnConfig> fStream = MagnoliaConfiguration.getConfig().getFunctions().stream();
        Stream <ExternalFnConfig> pStream = MagnoliaConfiguration.getConfig().getProcedures().stream();

        return Stream.concat(pStream, fStream);
    }

    public class ResultContainer {
        public Object result;
        public ResultContainer(Object thingy) {
            result = thingy;
        }
    }

    @Procedure(name = "magnolia.proc", mode = Mode.WRITE)
    @Description("CALL magnolia.proc('procname', arguments) | call a magnolia procedure by name, with a given argument")
    public Stream<ResultContainer> proc(final @Name("procName") String procName, final @Name("arguments") Object arguments) throws IOException {
        initializeIfNeeded();

        try {
            log.info("Inside of proc, looking for " + procName);
            initializeIfNeeded();
            // Look up function, instantiate, run, and return its results.
            ExternalFnConfig config = MagnoliaConfiguration.getConfig().getProcedureByName(procName);

            if (config == null) {
                throw new RuntimeException("No such dynamic magnolia function by the name " + procName);
            }

            ExternalFn fn = new ExternalFn(config);

            Value v = fn.invoke(arguments, db, log);

            // Procedures must return strings, so we have to check this proc did the right thing.
            assert(v.isHostObject());
            Object result = v.asHostObject();
            assert (result instanceof java.util.stream.Stream);

            // Map these into result containers as a type hack around the uncertainty of what comes back
            // from the dynamic procedure.  Unfortunately neo4j procs require a stream of concrete types,
            // and this is how we force the issue.
            return ((Stream)result).map(obj -> new ResultContainer(obj));
        } catch (Exception exc) {
            exc.printStackTrace();
            throw exc;
        }
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
