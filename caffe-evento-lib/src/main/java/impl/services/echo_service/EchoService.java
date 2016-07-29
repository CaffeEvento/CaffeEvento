package impl.services.echo_service;

import api.events.Event;
import api.events.EventHandler;
import api.events.EventSource;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import impl.events.EventImpl;
import impl.events.EventSourceImpl;
import impl.services.AbstractService;

/**
 * Created by eric on 7/29/16.
 */
public class EchoService extends AbstractService {
    private final EventSource eventGenerator = new EventSourceImpl();
    EchoService(EventQueueInterface eventQueueInterface){
        super(eventQueueInterface);
        getEventQueueInterface().addEventSource(eventGenerator);
        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventType("ECHO")
                .hasDataKey("Message")
                .eventHandler(event -> {
                    Event.decodeEvent(event.getEventField("Message"))
                            .ifPresent(event1 -> eventGenerator.registerEvent(new EventImpl(event1)));
                })
                .build());
    }
}
