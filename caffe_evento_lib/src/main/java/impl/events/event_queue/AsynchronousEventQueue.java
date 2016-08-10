package impl.events.event_queue;

import api.events.Event;
import api.events.EventHandler;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by chris on 7/21/16.
 */
public class AsynchronousEventQueue extends AbstractEventQueue {
    private ExecutorService myExecutor;
    private Consumer<Event> defaultHandler;

    public AsynchronousEventQueue(ExecutorService e) {
        this(e, c -> {});
    }

    public AsynchronousEventQueue(ExecutorService e, Consumer<Event> defaultHandler) {
        this.myExecutor = e;
        this.defaultHandler = defaultHandler;
    }

    public AsynchronousEventQueue(int numExecutors) {
        this(Executors.newFixedThreadPool(numExecutors), e -> {});
    }

    public AsynchronousEventQueue() {
        this(Runtime.getRuntime().availableProcessors() - 1);
    }

    @Override
    public void receiveEvent(Event e) {
        List<Future<Boolean>> results = getEventHandlers().stream()
                .map(handler -> (Callable<Boolean>) () -> processEvent(handler, e))
                .map(myExecutor::submit)
                .collect(Collectors.toList());

        new Thread() {
            public void run() {
                boolean anyRan = results.parallelStream().map(f -> {
                    try {
                        return f.get();
                    } catch (InterruptedException | ExecutionException e1) {
                        e1.printStackTrace();
                        return false;
                    }
                }).anyMatch(r -> r);
                if (!anyRan) {
                    defaultHandler.accept(e);
                }
            }
        }.run();
    }

    private boolean processEvent(EventHandler handler, Event e) {
        if (handler.getHandlerCondition().test(e)) {
            handler.handleEvent(e);
            return true;
        }
        return false;
    }
}
