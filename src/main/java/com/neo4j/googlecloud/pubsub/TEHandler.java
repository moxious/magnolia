package com.neo4j.googlecloud.pubsub;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.kernel.impl.logging.LogService;

import java.util.concurrent.ExecutorService;

public class TEHandler implements TransactionEventHandler {

    public static GraphDatabaseService db;
    private static ExecutorService ex;
    public static LogService logsvc;

    public TEHandler(GraphDatabaseService graphDatabaseService, ExecutorService executor, LogService logsvc) {
        db = graphDatabaseService;
        ex = executor;
        this.logsvc = logsvc;
    }

    @Override
    public Object beforeCommit(TransactionData transactionData) throws Exception {
        return null;
    }

    @Override
    public void afterCommit(TransactionData transactionData, Object o) {
        // By default the TX handler does nothing; if configured, it publishes
        // changes.  The number of changes can be large, so sometimes it's desirable
        // and sometimes not.
        Boolean publishChanges = PubsubConfiguration.get("publishChanges", false);

        if (publishChanges)
            ex.submit(new TransactionStreamer(transactionData, db, logsvc));
    }

    @Override
    public void afterRollback(TransactionData transactionData, Object o) {

    }
}
