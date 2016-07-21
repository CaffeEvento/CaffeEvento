package impl.events.event_queue.event_queue_interface;

import api.events.EventHandler;
import api.events.EventSource;
import api.lib.EmbeddedServletServer;
import impl.events.EventSourceImpl;
import impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import impl.lib.servlet_server.EmbeddedServletServerImpl;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * TODO: this is all probably mostly useless
 * Created by chris on 7/14/16.
 */
public class RemoteEventQueueInterface extends EventQueueInterfaceImpl {
    private String localEventDestination;
    private EmbeddedServletServer server;
    private String serverId;
    private EventSource eventGenerator;

    public RemoteEventQueueInterface(ServletContextHandler handler) {
        eventGenerator = new EventSourceImpl();
        addEventSource(eventGenerator);
        server = new EmbeddedServletServerImpl(handler);
//        server.addServletConsumer("/receiveEvent", (req, res) -> {
//            try {
//                eventGenerator.registerEvent(Event.decodeEvent(req.getReader()));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
    }

    public void connectToServer(String serverUrl) {
        // TODO: get the server id

        // TODO: set the local ip
    }

    @Override
    public void addEventHandler(EventHandler theEventHandler) {
        theEventHandler.addIpDestination(localEventDestination);

        // TODO: Send create event handler to the server
        // Send the Create event handler event

        // Send CREATE_EVENT_HANDLER_EVENT
        // Needs to have a server id so that we know which server should handle it.
        // if type == CREATE_EVENT_HANDLER_EVENT && serverId == my-server-id
        ///  - then we want to handle the event
        String eventHandlerData = theEventHandler.encodeToJson();
        // Send the event handler data, with the server data

        super.addEventHandler(theEventHandler);
    }

    // TODO: Create a HTTP server that handles the events, send them to receiveEvent
    // HTTP server should also have a getServerId thing

    // Should also have the ability ot handle CREATE_EVENT_HANDLER_EVENT
    //
}
