package impl.events.event_queue;

import api.events.Event;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import impl.events.event_queue.AbstractEventQueue;
import impl.lib.optional.OptionalConsumer;
import impl.services.AbstractService;

import java.util.function.Consumer;

/**
 * Created by eric on 7/27/16.
 */
public class FirstHandlerOnly extends AbstractEventQueue {
    Consumer<Event> defaultHandler;

    FirstHandlerOnly() {
        defaultHandler = event->{};
    }

    FirstHandlerOnly(Consumer<Event> defaultHandler) {
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
