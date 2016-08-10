package test_util;

import api.events.*;
import impl.events.EventHandlerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by chris on 7/9/16.
 */
public class EventCollector{
    private List<Event> collectedEvents = new ArrayList<>();
    private EventHandler handler;

    public EventCollector() {
        handler = EventHandlerImpl.create().eventHandler(collectedEvents::add).build();
    }

    public EventHandler getHandler() {
        return handler;
    }

    public List<Event> findEventsWithName(String name) {
        return collectedEvents.stream().filter(e -> e.getEventName().equals(name)).collect(Collectors.toList());
    }

    public List<Event> findEventsWithType(String type) {
        return collectedEvents.stream().filter(e -> e.getEventType().equals(type)).collect(Collectors.toList());
    }

    public List<Event> findEventsWithId(UUID id) {
        return collectedEvents.stream().filter(i -> i.getEventId().equals(id)).collect(Collectors.toList());
    }

    public List<Event> getCollectedEvents() {
        return new ArrayList<>(collectedEvents);
    }

    public void printState() {
        getCollectedEvents().stream().map(Event::encodeEvent).forEach(System.out::println);
    }
}
