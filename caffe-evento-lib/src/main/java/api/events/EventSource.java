package api.events;

import java.util.UUID;

/**
 * Created by chris on 7/13/16.
 */
public interface EventSource {
    void addListener(EventSink theEventSink);

    void removeListener(EventSink theEventSink);

    void registerEvent(Event theEvent);

    UUID getEventSourceId();
}
