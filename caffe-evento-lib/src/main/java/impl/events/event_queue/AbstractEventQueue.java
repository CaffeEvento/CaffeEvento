package impl.events.event_queue;

import api.events.EventHandler;
import api.events.event_queue.EventQueue;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.events.EventSource;
import api.services.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 7/21/16.
 */
public abstract class AbstractEventQueue implements EventQueue {
    protected List<EventHandler> eventHandlers = new ArrayList<>();
    protected List<EventSource> eventSources = new ArrayList<>();
    private List<EventQueueInterface> eventQueueInterfaces = new ArrayList<>();

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
