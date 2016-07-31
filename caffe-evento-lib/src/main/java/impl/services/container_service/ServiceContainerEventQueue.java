package impl.services.container_service;

import api.events.Event;
import api.events.EventHandler;
import api.events.EventSource;
import api.events.event_queue.EventQueue;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.services.Service;
import impl.events.EventHandlerImpl.EventHandlerBuilder;
import impl.events.EventSourceImpl;
import impl.services.AbstractService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by eric on 7/28/16.
 */
public abstract class ServiceContainerEventQueue implements EventQueue, Service{
    protected static final EventSource elevateGenerator = new EventSourceImpl();
    private final Service delegateService;
    private final EventQueue delegateEventQueue;
    protected final List<EventHandler> pullHandlers = new ArrayList<>();

    public ServiceContainerEventQueue(EventQueueInterface eventQueueInterface, Function<Consumer<Event>, EventQueue> internalQueueGenerator) {
        delegateEventQueue = internalQueueGenerator.apply(this::elevate);
        delegateService = new AbstractService(eventQueueInterface) {
            @Override
            public EventQueueInterface getEventQueueInterface() {
                return super.getEventQueueInterface();
            }
        };
        getEventQueueInterface().addEventSource(elevateGenerator);
        pullCriteria().forEach(pullHandler->pullHandlers.add(pullHandler.cloneOnlyCriteria().eventHandler(this::receiveEvent).build()));
        pullHandlers.forEach(pullHandler->getEventQueueInterface().addEventHandler(pullHandler));
    }

    //Internal Methods

    /**
     * elevate(Event event) is the default handler of the EventQueue if the returned EventQueue has a default method
     * @param event the event to be elevated to the next level of EventQueue, elevate may elect to modify or encapsulate event
     */
    abstract protected void elevate(Event event);

    /**
     * pullCriteria() sets the criteria for the Service to register external events with it's internal EventQueue
     * the EventHandlerBuilder that is returned by pullCriteria is copied and all handlers are scrubbed from it
     * @return EventHandlerBuilder
     */
    abstract protected List<EventHandlerBuilder> pullCriteria();


    //EventQueue: receiveEvent()
    @Override
    public void receiveEvent(Event event) {
        delegateEventQueue.receiveEvent(event);
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
}
