package api.events;

import impl.events.EventHandlerImpl;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * Created by chris on 7/13/16.
 */
public interface EventHandler {
    UUID getEventHandlerId();
    Predicate<Event> getHandlerCondition();
    void handleEvent(Event theEvent);
    String encodeToJson();
    void addIpDestination(String uri);

    static EventHandler fromJson(String json) {
        return EventHandlerImpl.fromJson(json);
    }

    static EventHandlerImpl.EventHandlerBuilder create() {
        return EventHandlerImpl.create();
    }
}
