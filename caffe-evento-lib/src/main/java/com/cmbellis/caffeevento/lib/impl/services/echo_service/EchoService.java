package com.cmbellis.caffeevento.lib.impl.services.echo_service;

import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.events.EventHandler;
import com.cmbellis.caffeevento.lib.api.events.EventSource;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.api.utils.EventBuilder;
import com.cmbellis.caffeevento.lib.impl.events.EventImpl;
import com.cmbellis.caffeevento.lib.impl.events.EventSourceImpl;
import com.cmbellis.caffeevento.lib.impl.services.AbstractService;

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
