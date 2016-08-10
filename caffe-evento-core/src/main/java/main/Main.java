package main;

import api.service_loader.CEServiceLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by chris on 8/9/16.
 */
public class Main {
    // TODO: we are going to need a "context" class that all these classes access in order to get resources
    public static final String WATCH_DIR_VAR = "caffeevento.watch_dir";
    private static ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static Map<Path, URLClassLoader> childrenClassLoaders = new HashMap<>();

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

        // TODO: Remove - test to see what the classpath looks like
//        scheduler.scheduleAtFixedRate(() -> printLoadedClasses(".*caffe.*"), 1, 2, TimeUnit.SECONDS);

        /*
         TODO: poll the ServiceProvider API to see if there are any services that can be provided
         So then we can determine the overall services that are available!
         */
        scheduler.scheduleAtFixedRate(Main::printLoadedServices, 1, 2, TimeUnit.SECONDS);
    }

    private static void printLoadedServices() {
        System.out.println("Printing loaded services");
        childrenClassLoaders.entrySet().stream()
                .map(Map.Entry::getValue)
                .forEach(classLoader -> {
                    ServiceLoader<CEServiceLoader> serviceLoader = ServiceLoader.load(CEServiceLoader.class, classLoader);
                    serviceLoader.forEach(Main::printLoaderInfo);
                });
    }

    private static void printLoaderInfo(CEServiceLoader loader) {
        System.out.println("Loader to string: " + loader.toString());
        System.out.println("Loader class: " + loader.getClass().toString());
    }

    private static void printLoadedClasses(String filter) {
        URLClassLoader systemClassLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        System.out.println("--System Class Loader--");
        Stream.of(systemClassLoader.getURLs())
                .map(URL::getFile)
                .filter(url -> url.matches(filter))
                .forEach(System.out::println);

        System.out.println("--Children Class Loader--");
        childrenClassLoaders.entrySet().stream().map(Map.Entry::getValue)
                .flatMap(cl -> Stream.of(cl.getURLs()))
                .map(URL::getFile)
                .filter(url -> url.matches(filter))
                .forEach(System.out::println);

        System.out.println("-----------------------");
    }

    private static void handleAddJar(Path path) {
        // Remove the old child class loader to remove its reference
        // and remove it from scope so it could be garbage collected
        childrenClassLoaders.remove(path);

        // Do terrible, terrible things to make an array and get that damn url
        URL[] pathUrl = new URL[0];
        try {
            pathUrl = new URL[]{ path.toUri().toURL() };
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if(pathUrl.length == 0) {
            return;
        }

        // Create a CHILD class loader and keep a reference to it so it doesn't get unloaded
        URLClassLoader child = new URLClassLoader(pathUrl, ClassLoader.getSystemClassLoader());
        childrenClassLoaders.put(path, child);

        // Print an ungodly message for the mess that has just been made, looks like the floor of an emergency room
        // in here after a mass shooting...
        System.out.println("added jar: " + path);
    }

    private static void handleRemoveJar(Path path) {
        childrenClassLoaders.remove(path); // Remove so that this might get garbage collected
        System.out.println("removed jar: " + path);
    }

    private static void handleModifyJar(Path path) {
        handleAddJar(path);
    }
}
