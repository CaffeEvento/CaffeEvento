package impl.services.Container_Services;

import api.events.Event;
import api.events.EventHandler;
import api.events.EventSource;
import api.events.event_queue.EventQueue;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.services.Service;
import impl.events.EventHandlerImpl;
import impl.events.EventSourceImpl;
import impl.services.AbstractService;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * Created by eric on 7/28/16.
 */
public class ServiceContainerEventQueue implements EventQueue, Service{
    private final EventSource eventGenerator = new EventSourceImpl();
    private final Service delegateService;
    private final EventQueue delegateEventQueue;
    private final EventHandler handles;
    private final Predicate<Event> eventPredicate;

    ServiceContainerEventQueue(EventQueueInterface eventQueueInterface, EventQueue internalEventQueue, Predicate<Event> eventPredicate) {
        delegateEventQueue = internalEventQueue;
        delegateService = new AbstractService(eventQueueInterface) {
            @Override
            public EventQueueInterface getEventQueueInterface() {
                return super.getEventQueueInterface();
            }
        };
        getEventQueueInterface().addEventSource(eventGenerator);
        this.eventPredicate = eventPredicate;
        handles = new EventHandler() {
            private EventHandler delegate = EventHandler
                    .create()
                    .eventHandler(event -> receiveEvent(event))
                    .build();
            @Override
            public UUID getEventHandlerId() {
                return delegate.getEventHandlerId();
            }

            @Override
            public Predicate<Event> getHandlerCondition() {
                return eventPredicate;
            }

            @Override
            public void handleEvent(Event theEvent) {
                delegate.handleEvent(theEvent);
            }

            @Override
            public String encodeToJson() {
                return delegate.encodeToJson();
            }

            @Override
            public void addIpDestination(String uri) {
                delegate.addIpDestination(uri);
            }

            @Override
            public EventHandler getCopy() {
                return delegate.getCopy();
            }
        };
        getEventQueueInterface().addEventHandler(handles);
    }

    //EventQueue: receiveEvent()
    @Override
    public synchronized void receiveEvent(Event event) {
        if (eventPredicate.test(event)) {
            delegateEventQueue.receiveEvent(event);
        } else {
            elevate(event);
        }
    }

    //EventQueue: everything else
    @Override
    public void registerService(Service service) {
        delegateEventQueue.registerService(service);
    }
    @Override
    public void addEventHandler(EventHandler eventHandler) {
        delegateEventQueue.addEventHandler(eventHandler);
    }
    @Override
    public void addEventSource(EventSource eventSource) {
        delegateEventQueue.addEventSource(eventSource);
    }
    @Override
    public void unRegisterService(Service service) {
        delegateEventQueue.unRegisterService(service);
    }
    @Override
    public void addEventQueueInterface(EventQueueInterface eventQueueInterface) {
        delegateEventQueue.addEventQueueInterface(eventQueueInterface);
    }
    @Override
    public void removeEventQueueInterface(EventQueueInterface eventQueueInterface) {
        delegateEventQueue.removeEventQueueInterface(eventQueueInterface);
    }
    @Override
    public void removeEventSource(EventSource eventSource) {
        delegateEventQueue.removeEventSource(eventSource);
    }
    @Override
    public void removeEventHandler(EventHandler eventHandler) {
        delegateEventQueue.removeEventHandler(eventHandler);
    }

    //Service
    @Override
    public EventQueueInterface getEventQueueInterface(){
        return delegateService.getEventQueueInterface();
    }

    //Internal Methods
    public void elevate(Event event) {
        eventGenerator.registerEvent(event);
    }
}
