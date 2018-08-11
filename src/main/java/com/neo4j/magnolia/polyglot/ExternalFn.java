package com.neo4j.magnolia.polyglot;

import com.neo4j.magnolia.config.ExternalFnConfig;
import com.neo4j.magnolia.config.MagnoliaConfiguration;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class ExternalFn {
    public static final String artifactBase = "/Users/davidallen/hax/magnolia/external";

    protected String script = null;
    protected String lang = null;
    protected String name = null;

    public ExternalFn(String name, String filename, String language) throws IOException {
        System.out.println("ExternalFn(" + name + ", " + filename + ", " + language + ")");
        this.lang = language;
        this.name = name;
        Path fullPath = Paths.get(MagnoliaConfiguration.getConfig().getBasePath(), filename);
        byte[] encoded = Files.readAllBytes(fullPath);
        this.script = new String(encoded, "utf-8");
    }

    public ExternalFn(String lang, String scriptContents) throws IOException {
        this.name = "anonymous-" + new Date();
        this.lang = lang;
        this.script = scriptContents;
    }

    public ExternalFn(ExternalFnConfig item) throws IOException {
        this(item.getName(), item.getFile(), item.getLanguage());
    }

    /**
     * This function creates a new Graal context for the execution of some other scripted language function, and returns
     * the value.
     * @param arguments the arguments you want passed to the function
     * @param svc for access to the neo4j api
     * @param log for access to logging services
     * @return a polyglot value that results from the external function
     */
    public Value invoke(Object arguments, GraphDatabaseService svc, Log log) {
        if (log != null) {
            log.info("Executing " + name + "/" + lang + " with arguments " + arguments + " of type " + (arguments != null ? arguments.getClass() : null));
        }
        Context context = Context.create();
        Value v = context.getPolyglotBindings();
        v.putMember("graph", svc);
        v.putMember("log", log);
        v.putMember("arguments", arguments);

        final Value jsResult = context.eval(this.lang, this.script);
        System.out.println(jsResult);
        return jsResult;
    }
}
