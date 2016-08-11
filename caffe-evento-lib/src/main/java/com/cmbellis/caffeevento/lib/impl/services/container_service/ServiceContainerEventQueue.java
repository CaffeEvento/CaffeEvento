package com.cmbellis.caffeevento.lib.impl.services.container_service;

import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.events.EventHandler;
import com.cmbellis.caffeevento.lib.api.events.EventSource;
import com.cmbellis.caffeevento.lib.api.events.event_queue.EventQueue;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.api.services.Service;
import com.cmbellis.caffeevento.lib.impl.events.EventHandlerImpl.EventHandlerBuilder;
import com.cmbellis.caffeevento.lib.impl.events.EventSourceImpl;
import com.cmbellis.caffeevento.lib.impl.services.AbstractService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by eric on 7/28/16.
 */
public abstract class ServiceContainerEventQueue implements EventQueue, Service {
    protected Log log;
    protected static final EventSource elevateGenerator = new EventSourceImpl();
    private final Service delegateService;
    private final EventQueue delegateEventQueue;
    protected final List<EventHandler> pullHandlers;

    public ServiceContainerEventQueue(EventQueueInterface eventQueueInterface, Function<Consumer<Event>, EventQueue> internalQueueGenerator) {
        log = LogFactory.getLog(getClass());
        delegateEventQueue = internalQueueGenerator.apply(this::elevate);
        delegateService = new AbstractService(eventQueueInterface) {
            @Override
            public EventQueueInterface getEventQueueInterface() {
                return super.getEventQueueInterface();
            }
        };
        getEventQueueInterface().addEventSource(elevateGenerator);
        //TODO: Repair this section, this part adds the possibility of double registering events on the event queue by registering all the event handlers on a list.
        // This is only be prevented if the handlers are registered to a FirstHandlerOnly queue or all handlers registered return a mutually exclusive predicate.
        pullHandlers = pullCriteria().stream()
                .map(EventHandlerBuilder::cloneOnlyCriteria)
                .map(eventHandlerBuilder -> eventHandlerBuilder.eventHandler(this::receiveEvent))
                .map(EventHandlerBuilder::build)
                .collect(Collectors.toList());
        pullHandlers.forEach(getEventQueueInterface()::addEventHandler);
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
