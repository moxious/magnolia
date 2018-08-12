package com.neo4j.magnolia.polyglot;

import com.neo4j.magnolia.config.ExternalFnConfig;
import com.neo4j.magnolia.config.MagnoliaConfiguration;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class ExternalFn {
    private String lang = null;
    private String name = null;
    private Source source = null;

    public ExternalFn(String name, String filename, String language) throws IOException {
        System.out.println("ExternalFn(" + name + ", " + filename + ", " + language + ")");
        this.lang = language;
        this.name = name;

        // Right now this Source approach means that the file gets reloaded from disk every time it's executed
        // TODO -- a caching approach
        // Graal can cache this so it doesn't get reparsed every time.  Tradeoff between on-disk freshness, and
        // not wasting cycles.  Maybe an LRU cache of ExternalFn objects, that can get invalidated by the
        // DirectoryWatcher.
        Path fullPath = Paths.get(MagnoliaConfiguration.getConfig().getBasePath(), filename);
        source = Source.newBuilder(language, fullPath.toFile()).build();
    }

    public ExternalFn(String lang, String scriptContents) throws IOException {
        this.name = "anonymous-" + new Date();
        this.lang = lang;

        // Build directly from string input.
        source = Source.newBuilder(lang, scriptContents, name).build();
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

        // See docs for java interoperability; the allowing host access here is critical.
        // https://github.com/graalvm/graaljs/blob/master/docs/user/JavaInterop.md#enabling-java-interoperability
        try (Context context = Context.newBuilder(lang)
                .allowIO(true)
                .allowCreateThread(false)
                .allowHostClassLoading(true)
                .allowHostAccess(true)
                .build()) {
            Value v = context.getPolyglotBindings();
            v.putMember("graph", svc);
            v.putMember("log", log);
            v.putMember("arguments", arguments);

            final Value jsResult = context.eval(source);
            System.out.println(jsResult);
            return jsResult;
        }
    }
}
