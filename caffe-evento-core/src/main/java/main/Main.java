package main;

import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by chris on 8/9/16.
 */
public class Main {
    // TODO: we are going to need a "context" class that all these classes access in order to get resources
    public static final String WATCH_DIR_VAR = "caffeevento.watch_dir";
    private static ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    public static void main(String[] args) throws Exception {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        PathMatcher jarMatcher = FileSystems.getDefault().getPathMatcher("glob:**.jar");
        Path dir = Paths.get(System.getProperty(WATCH_DIR_VAR, "./bundles"));
        // Register this before doing some work on the directory
        // so that if anything is added after we look then it will be added in the event loop
        dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        // Add all the files that are already there
        Files.walk(dir)
                .map(Path::toAbsolutePath)
                .filter(jarMatcher::matches)
                .forEach(Main::handleAddJar);

        // Submit long-running job to thread pool to handle all new jars being added
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        WatchKey key = watcher.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind == OVERFLOW) {
                                continue;
                            }
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path loc = ev.context().toAbsolutePath();
                            if (jarMatcher.matches(loc)) {
                                if (kind == ENTRY_DELETE) {
                                    handleRemoveJar(loc);
                                } else if (kind == ENTRY_MODIFY) {
                                    handleModifyJar(loc);
                                }
                            }
                        }
                        key.reset();
                    } catch (Exception e) {
                        System.out.println("I AM A TERRIBLE PERSON AND I SHOULD FEEL BAD ABOUT THIS");
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private static void handleAddJar(Path path) {
        System.out.println("added jar: " + path);
    }

    private static void handleRemoveJar(Path path) {
        System.out.println("removed jar: " + path);
    }

    // Remove the old jar entry? (maybe)
    private static void handleModifyJar(Path path) {
        handleAddJar(path);
    }
}
