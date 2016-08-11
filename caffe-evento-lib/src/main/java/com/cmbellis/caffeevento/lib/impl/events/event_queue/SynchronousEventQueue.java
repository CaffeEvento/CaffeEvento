package com.cmbellis.caffeevento.lib.impl.events.event_queue;

import com.cmbellis.caffeevento.lib.api.events.*;

import java.util.function.Consumer;

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
        long numHandlers = getEventHandlers().stream()
                .filter(handler -> handler.getHandlerCondition().test(e))
                .peek(handler -> handler.handleEvent(e))
                .count();
        if(numHandlers <= 0) {
            defaultHandler.accept(e);
        }
    }
}
