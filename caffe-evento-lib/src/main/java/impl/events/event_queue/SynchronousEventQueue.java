package impl.events.event_queue;

import api.events.*;
import impl.events.event_queue.AbstractEventQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by chris on 7/1/16.
 */
public class SynchronousEventQueue extends AbstractEventQueue {
    private Consumer<Event> defaultHandler;

    public SynchronousEventQueue() {
        this(c->{});
    }

    public SynchronousEventQueue(Consumer<Event> defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    @Override
    public synchronized void receiveEvent(Event e) {
        if (getEventHandlers().stream()
                .filter(handler -> handler.getHandlerCondition().test(e))
                .peek(handler -> handler.handleEvent(e))
                .reduce(true, (b,h)->false, (c,d)->c&&d)){
            defaultHandler.accept(e);
        }
    }
}
