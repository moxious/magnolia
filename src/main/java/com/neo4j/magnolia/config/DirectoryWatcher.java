package com.neo4j.magnolia.config;

import java.io.IOException;
import java.nio.file.*;

public class DirectoryWatcher implements Runnable {
    private WatchService watchService;
    private Path basePath;

    public DirectoryWatcher(String dirName) throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        basePath = Paths.get(dirName);
        basePath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

    }

    public void run() {
        try {
            WatchKey watchKey;
            while ((watchKey = watchService.take()) != null) {
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    System.out.println(
                            "Event kind:" + event.kind()
                                    + ". File affected: " + event.context() + ".");
                }
                watchKey.reset();
            }
        } catch(InterruptedException ie) {
            ie.printStackTrace();
        }
    }
}
