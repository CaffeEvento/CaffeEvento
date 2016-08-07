package impl.services.echo_service;

import api.events.Event;
import api.events.EventHandler;
import api.events.EventSource;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.utils.EventBuilder;
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
                .hasDataKey("MESSAGE")
                .eventHandler(event -> {
                    Event.decodeEvent(event.getEventField("MESSAGE"))
                            .map(EventImpl::new)
                            .ifPresent(eventGenerator::registerEvent);
                })
                .build());
    }

    public static EventBuilder createEchoEvent(Event message) {
        return EventBuilder.create()
                .name("Echo " + message.getEventName())
                .type("ECHO")
                .data("MESSAGE", message.encodeEvent());
    }
}
