package api.events;

import com.google.gson.JsonSyntaxException;
import impl.events.EventHandlerImpl;

import java.util.Optional;
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
    EventHandler getCopy();

    static Optional<EventHandler> fromJson(String json) {
            return EventHandlerImpl.fromJson(json);
    }

    static EventHandlerImpl.EventHandlerBuilder create() {
        return EventHandlerImpl.create();
    }
}
