package impl.events.event_queue;

import api.events.Event;
import api.events.EventHandler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by chris on 7/21/16.
 */
public class AsynchronousEventQueue extends AbstractEventQueue {
    private Executor myExecutor;

    public AsynchronousEventQueue(Executor e) {
        this.myExecutor = e;
    }

    public AsynchronousEventQueue(int numExecutors) {
        this(Executors.newFixedThreadPool(numExecutors));
    }

    public AsynchronousEventQueue() {
        this(Runtime.getRuntime().availableProcessors() - 1);
    }

    @Override
    public void receiveEvent(Event e) {
        getEventHandlers().stream()
                .map(handler -> (Runnable) () -> processEvent(handler, e))
                .forEach(myExecutor::execute);
    }

    private void processEvent(EventHandler handler, Event e) {
        if (handler.getHandlerCondition().test(e)) {
            handler.handleEvent(e);
        }
    }
}
