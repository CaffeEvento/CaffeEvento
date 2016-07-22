package impl.services.remote_service;

import api.events.Event;
import api.events.EventHandler;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.events.EventSource;
import com.google.gson.JsonSyntaxException;
import impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import impl.events.EventSourceImpl;
import api.lib.EmbeddedServletServer;
import impl.lib.servlet_server.EmbeddedServletServerImpl;
import impl.services.AbstractService;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.rmi.Remote;
import java.util.UUID;

/**
 * Created by chris on 7/14/16.
 */
public final class RemoteServerService extends AbstractService {
    private UUID serverId = UUID.randomUUID();
    private EventSource eventGenerator;

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
                    EventHandler.fromJson(event.getEventField("eventHandlerDetails"))
                            .ifPresent(h->getEventQueueInterface().addEventHandler(h));
                    //TODO: Code does not log that it recieved a bad event handler when it runs this section, please advise.
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
                        //TODO: Log this error
                    }
                })
                .build());

        // Register the event if it is well formatted.
        server.addService(name, serverId, "/receiveEvent", (req, res) -> Event.decodeEvent(req.getReader()).ifPresent(eventGenerator::registerEvent));
    }
}
