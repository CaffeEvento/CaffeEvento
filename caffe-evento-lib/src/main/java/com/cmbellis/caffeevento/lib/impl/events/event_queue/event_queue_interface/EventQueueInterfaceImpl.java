package com.cmbellis.caffeevento.lib.impl.events.event_queue.event_queue_interface;

import com.cmbellis.caffeevento.lib.api.events.EventHandler;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterfaceChangedListener;
import com.cmbellis.caffeevento.lib.api.events.EventSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created by chris on 7/1/16.
 */
public class EventQueueInterfaceImpl implements EventQueueInterface {

    private List<EventQueueInterfaceChangedListener> eventQueueInterfaceChangedListeners = new ArrayList<>();
    private List<EventSource> eventSources = new ArrayList<>();
    private List<EventHandler> eventHandlers = new ArrayList<>();

    protected Log log;

    @Override
    public List<EventSource> getEventSources() {
        return new ArrayList<>(eventSources);
    }

    @Override
    public List<EventHandler> getEventHandlers() {
        return new ArrayList<>(eventHandlers);
    }

    public EventQueueInterfaceImpl() {
        log = LogFactory.getLog(getClass());
    }

    @Override
    public void addEventQueueInterfaceChangedListener(EventQueueInterfaceChangedListener theEventQueueInterfaceChangedListener) {
        eventQueueInterfaceChangedListeners.add(theEventQueueInterfaceChangedListener);
    }

    @Override
    public void removeEventQueueInterfaceChangedListener(EventQueueInterfaceChangedListener theEventQueueInterfaceChangedListener) {
        eventQueueInterfaceChangedListeners.remove(theEventQueueInterfaceChangedListener);
    }

    @Override
    public void removeEventSource(UUID id) {
        eventSources.stream().filter(s -> s.getEventSourceId().equals(id)).findFirst()
                .ifPresent(this::removeEventSource);
    }

    @Override
    public void addEventSource(EventSource theEventSource) {
        eventSources.add(theEventSource);
        updateServiceChangedListeners(l -> l.addEventSource(theEventSource));
    }

    @Override
    public void removeEventSource(EventSource theEventSource) {
        eventSources.remove(theEventSource);
        updateServiceChangedListeners(l -> l.removeEventSource(theEventSource));
    }

    @Override
    public void removeEventHandler(UUID id) {
        eventHandlers.stream().filter(h -> h.getEventHandlerId().equals(id)).findFirst()
                .ifPresent(this::removeEventHandler);
    }

    @Override
    public void addEventHandler(EventHandler theEventHandler) {
        eventHandlers.add(theEventHandler);
        updateServiceChangedListeners(l -> l.addEventHandler(theEventHandler));
    }

    @Override
    public void removeEventHandler(EventHandler theEventHandler) {
        eventHandlers.remove(theEventHandler);
        updateServiceChangedListeners(l -> l.removeEventHandler(theEventHandler));
    }

    private void updateServiceChangedListeners(Consumer<EventQueueInterfaceChangedListener> serviceChange) {
        eventQueueInterfaceChangedListeners.forEach(serviceChange);
    }
}
