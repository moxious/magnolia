package com.neo4j.googlecloud.pubsub;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.impl.spi.KernelContext;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;
import org.neo4j.kernel.impl.logging.LogService;
import org.neo4j.logging.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterTransactionEventHandlerExtensionFactory extends KernelExtensionFactory<RegisterTransactionEventHandlerExtensionFactory.Dependencies> {
    private Log logger;

    @Override
    public Lifecycle newInstance(KernelContext kernelContext, final Dependencies dependencies) {
        return new LifecycleAdapter() {
            LogService log = dependencies.log();

            private TEHandler handler;
            private ExecutorService executor;

            @Override
            public void start() {
                PubSubConnector.initialize(log);

                System.out.println("STARTING trigger watcher");
                executor = Executors.newFixedThreadPool(2);
                PubsubConfiguration.initialize(dependencies.getGraphDatabaseAPI());
                handler = new TEHandler(dependencies.getGraphDatabaseService(), executor, log);
                dependencies.getGraphDatabaseService().registerTransactionEventHandler(handler);
            }

            @Override
            public void shutdown() {
                System.out.println("STOPPING trigger watcher");
                executor.shutdown();
                dependencies.getGraphDatabaseService().unregisterTransactionEventHandler(handler);
            }
        };
    }

    interface Dependencies {
        GraphDatabaseService getGraphDatabaseService();
        GraphDatabaseAPI getGraphDatabaseAPI();
        LogService log();
    }

    public RegisterTransactionEventHandlerExtensionFactory() {
        super("registerTransactionEventHandler");
        this.logger = null;
    }

}
