package com.cmbellis.caffeevento.lib.impl.services.remote_service;

import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.events.EventHandler;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.api.events.EventSource;
import com.cmbellis.caffeevento.lib.impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import com.cmbellis.caffeevento.lib.impl.events.EventSourceImpl;
import com.cmbellis.caffeevento.lib.api.lib.EmbeddedServletServer;
import com.cmbellis.caffeevento.lib.impl.lib.optional.OptionalConsumer;
import com.cmbellis.caffeevento.lib.impl.lib.servlet_server.EmbeddedServletServerImpl;
import com.cmbellis.caffeevento.lib.impl.services.AbstractService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.UUID;

/**
 * Created by chris on 7/14/16.
 */
public final class RemoteServerService extends AbstractService {
    private UUID serverId = UUID.randomUUID();
    private EventSource eventGenerator;
    private Log log = LogFactory.getLog(getClass());

    public RemoteServerService(String name, ServletContextHandler handler) {
        this(name, new EventQueueInterfaceImpl(), handler);
    }

    public UUID getServerId() {
        return serverId;
    }

    public RemoteServerService(String name, EventQueueInterface eventQueueInterface, ServletContextHandler handler) {
        super(eventQueueInterface);
        eventGenerator = new EventSourceImpl();
        EmbeddedServletServer server = new EmbeddedServletServerImpl(handler);
        getEventQueueInterface().addEventSource(eventGenerator);

        // Create event handler event
        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventType("CREATE_EVENT_HANDLER")
                .eventData("serverId", serverId.toString())
                .hasDataKey("eventHandlerDetails")
                .eventHandler(event -> {
                    OptionalConsumer.of(EventHandler.fromJson(event.getEventField("eventHandlerDetails")))
                            .ifPresent(h->getEventQueueInterface().addEventHandler(h))
                            .ifNotPresent(() -> log.debug("received unparsable event"));
                })
                .build());

        // Remove event handler event
        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventType("REMOVE_EVENT_HANDLER")
                .eventData("serverId", serverId.toString())
                .hasDataKey("eventHandlerId")
                .eventHandler(event -> {
                    try {
                        getEventQueueInterface().removeEventHandler(UUID.fromString(event.getEventField("eventHandlerId")));
                    }catch(IllegalArgumentException e) {
                        log.debug("Recieved unparseable UUID");
                    }
                })
                .build());

        // Register the event if it is well formatted.
        server.addService(name, serverId, "/receiveEvent", (req, res) -> Event.decodeEvent(req.getReader()).ifPresent(eventGenerator::registerEvent));
    }
}
