package com.neo4j.magnolia.googlecloud.pubsub;

import com.neo4j.magnolia.config.MagnoliaConfiguration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.impl.spi.KernelContext;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;
import org.neo4j.kernel.impl.logging.LogService;
import org.neo4j.logging.Log;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterTransactionEventHandlerExtensionFactory extends KernelExtensionFactory<RegisterTransactionEventHandlerExtensionFactory.Dependencies> {
    private Log logger;

    @Override
    public Lifecycle newInstance(KernelContext kernelContext, final Dependencies dependencies) {
        return new LifecycleAdapter() {
            LogService logSvc = dependencies.log();
            Log log = logSvc.getUserLog(RegisterTransactionEventHandlerExtensionFactory.class);

            private TEHandler handler;
            private ExecutorService executor;

            @Override
            public void start() {
                log.warn("Registering transaction handler");

                PubSubConnector.initialize(logSvc);

                System.out.println("STARTING trigger watcher");
                executor = Executors.newFixedThreadPool(2);

                try {
                    MagnoliaConfiguration.initialize(dependencies.getGraphDatabaseAPI());
                    PubsubConfiguration.initialize(dependencies.getGraphDatabaseAPI());
                } catch (IOException exc) {
                    logSvc.getUserLog(RegisterTransactionEventHandlerExtensionFactory.class).error("Failed to initialize", exc);
                }

                handler = new TEHandler(dependencies.getGraphDatabaseService(), executor, logSvc);
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
