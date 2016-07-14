package impl.event_queue;

import api.event_queue.Event;
import api.event_queue.EventQueue;
import api.event_queue.EventSink;
import api.event_queue.EventSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by chris on 7/1/16.
 */
public class EventSourceImpl implements EventSource {
    private UUID eventSourceId = UUID.randomUUID();

    private List<EventSink> eventSinks = new ArrayList<>();

    @Override
    public void addListener(EventSink theEventSink) {
        eventSinks.add(theEventSink);
    }

    @Override
    public void removeListener(EventSink theEventSink) {
        eventSinks.remove(theEventSink);
    }

    @Override
    public void registerEvent(Event theEvent) {
        eventSinks.forEach(eventSink -> eventSink.receiveEvent(theEvent));
    }

    @Override
    public final UUID getEventSourceId() {
        return eventSourceId;
    }
}
