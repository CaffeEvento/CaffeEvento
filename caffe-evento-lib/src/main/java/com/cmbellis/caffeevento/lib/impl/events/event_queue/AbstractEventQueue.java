package com.cmbellis.caffeevento.lib.impl.events.event_queue;

import com.cmbellis.caffeevento.lib.api.events.EventHandler;
import com.cmbellis.caffeevento.lib.api.events.event_queue.EventQueue;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.api.events.EventSource;
import com.cmbellis.caffeevento.lib.api.services.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 7/21/16.
 */
public abstract class AbstractEventQueue implements EventQueue {
    private List<EventHandler> eventHandlers = new ArrayList<>();
    private List<EventSource> eventSources = new ArrayList<>();
    private List<EventQueueInterface> eventQueueInterfaces = new ArrayList<>();

    protected List<EventHandler> getEventHandlers() {
        return new ArrayList<>(eventHandlers);
    }

    protected List<EventSource> getEventSources() {
        return new ArrayList<>();
    }

    @Override
    public void registerService(Service theService) {
        addEventQueueInterface(theService.getEventQueueInterface());
    }

    @Override
    public void addEventQueueInterface(EventQueueInterface theEventQueueInterface) {
        eventQueueInterfaces.add(theEventQueueInterface);
        theEventQueueInterface.getEventHandlers().forEach(this::addEventHandler);
        theEventQueueInterface.getEventSources().forEach(this::addEventSource);
        theEventQueueInterface.addEventQueueInterfaceChangedListener(this);
    }

    @Override
    public void unRegisterService(Service theService) {
        removeEventQueueInterface(theService.getEventQueueInterface());
    }

    @Override
    public void removeEventQueueInterface(EventQueueInterface theEventQueueInterface) {
        theEventQueueInterface.removeEventQueueInterfaceChangedListener(this);
        theEventQueueInterface.getEventHandlers().forEach(this::removeEventHandler);
        theEventQueueInterface.getEventSources().forEach(this::removeEventSource);
        eventQueueInterfaces.remove(theEventQueueInterface);
    }

    @Override
    public void addEventHandler(EventHandler theEventHandler) {
        eventHandlers.add(theEventHandler);
    }

    @Override
    public void removeEventHandler(EventHandler theEventHandler) {
        eventHandlers.remove(theEventHandler);
    }

    @Override
    public void addEventSource(EventSource theEventSource) {
        eventSources.add(theEventSource);
        theEventSource.addListener(this);
    }

    @Override
    public void removeEventSource(EventSource theEventSource) {
        eventSources.remove(theEventSource);
        theEventSource.removeListener(this);
    }
}
