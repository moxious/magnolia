package com.neo4j.magnolia.config;

import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.procedure.Context;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DirectoryWatcher implements Runnable {
    private WatchService watchService;
    private Path basePath;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> countdown = null;
    @Context GraphDatabaseAPI api;

    public DirectoryWatcher(String dirName) throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        basePath = Paths.get(dirName);
        basePath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        System.out.println("Watching " + dirName + " for changes");
    }

    /**
     * Lots of filesystem modifications can happen one after another.  Some apps which save files might trigger
     * 3 or 4 modify events.  So instead of reloading in response to an event, we schedule an event 1 sec after
     * the modify event.  If a clashing modify comes in, then the reload gets cancelled.  This way, when the last
     * in a string of rapid modify events comes in, config gets reloaded 1 sec later.
     */
    public void scheduleConfigurationReload() {
//        System.out.println("Scheduling configuration reload");
        if (countdown != null) {
//            System.out.println("Cancelling previous countdown");
            countdown.cancel(false);
        }

        countdown = scheduler.schedule(() -> {
            System.out.println("Magnolia dynamic configuration reload");
            try {
                MagnoliaConfiguration.initialize(api);
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }, 1, TimeUnit.SECONDS);
    }

    public void run() {
        String configFile = MagnoliaConfiguration.getConfigurationFilePath(api);

        try {
            WatchKey watchKey;

            while ((watchKey = watchService.take()) != null) {
                boolean relevantChange = false;

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    System.out.println(
                            "Event kind:" + event.kind()
                                    + ". File affected: " + event.context() + ".");
                }
                watchKey.reset();

                if (relevantChange) {
                    scheduleConfigurationReload();
                }
            }
        } catch(InterruptedException ie) {
            ie.printStackTrace();
        }
    }
}
