package com.neo4j.magnolia.polyglot;

import com.neo4j.magnolia.config.ExternalFnConfig;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExternalFn {
    public static final String artifactBase = "/Users/davidallen/hax/magnolia/external";

    protected String script = null;
    protected String lang = null;

    public ExternalFn(String name, String filename, String language) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(artifactBase, filename));
        this.script = new String(encoded, "utf-8");
        this.lang = language;
    }

    public ExternalFn(ExternalFnConfig item) throws IOException {
        this(item.getName(), item.getFile(), item.getLanguage());
    }

    public Value invoke(Object arguments, GraphDatabaseService svc, Log log) {
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
