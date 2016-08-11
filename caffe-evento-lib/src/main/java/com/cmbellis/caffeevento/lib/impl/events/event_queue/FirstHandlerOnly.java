package com.cmbellis.caffeevento.lib.impl.events.event_queue;

import com.cmbellis.caffeevento.lib.annotation.CEExport;
import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.impl.lib.optional.OptionalConsumer;

import java.util.function.Consumer;

/**
 * Created by eric on 7/27/16.
 */
@CEExport
public class FirstHandlerOnly extends AbstractEventQueue {
    Consumer<Event> defaultHandler;

    public FirstHandlerOnly() {
        defaultHandler = event->{};
    }

    public FirstHandlerOnly(Consumer<Event> defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    @Override
    public synchronized void receiveEvent(Event e) {
        OptionalConsumer.of(getEventHandlers().stream()
                .filter(handler -> handler.getHandlerCondition().test(e))
                .findFirst())
                .ifPresent(handler -> handler.handleEvent(e))
                .ifNotPresent(() -> defaultHandler.accept(e));
    }
}
