package impl.services.remote_service;

import api.events.*;
import api.events.event_queue.EventQueue;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.lib.EmbeddedServletServer;
import api.utils.EventBuilder;
import impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import impl.events.EventSourceImpl;
import impl.events.event_queue.SynchronousEventQueue;
import impl.lib.servlet_server.EmbeddedServletServerImpl;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import test_util.EventCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Created by chris on 7/16/16.
 */
public class RemoteServerServiceTest {
    private static final int port = 2345;
    private static final int rxPort = 2346;
    private Server sourceServer = new Server(port);
    private Server localServer = new Server(rxPort);
    private ServletContextHandler sourceContextHandler = new ServletContextHandler();
    private ServletContextHandler localContextHander = new ServletContextHandler();

    private EventQueue eventQueue = new SynchronousEventQueue();
    private EventQueueInterface eventQueueInterface = new EventQueueInterfaceImpl();
    private RemoteServerService instance = new RemoteServerService("Server Test", eventQueueInterface, sourceContextHandler);
    private HttpClient client;
    private EventCollector eventCollector = new EventCollector();
    private EventSource eventInjector = new EventSourceImpl();
    private EmbeddedServletServer receivingService = new EmbeddedServletServerImpl(localContextHander);
    private final List<Event> receivedEvents = new ArrayList<>();

    private UUID receivingId = UUID.randomUUID();
    private String callbackAddress = "http://localhost:"+rxPort+"/"+receivingId.toString()+"/receiveEvent";
    private String serverEventAddress;

    public void setUpServers() {
        sourceContextHandler.setContextPath("/");
        sourceServer.setHandler(sourceContextHandler);

        localContextHander.setContextPath("/");
        localServer.setHandler(localContextHander);
    }

    @Before
    public void setUp() throws Exception {
        client = HttpClients.createDefault();
        serverEventAddress = "http://localhost:"+port+"/"+instance.getServerId()+"/receiveEvent";
        eventQueue.registerService(instance);
        eventQueue.addEventSource(eventInjector);
        eventQueue.addEventHandler(eventCollector.getHandler());
        setUpServers();
        sourceServer.start();
        localServer.start();
        //TODO: confirm whether behavior of "Event.decodeEvent(req.getReader()).orElse(null)" matches desired behavior
        receivingService.addService("Test Service", receivingId, "/receiveEvent",
                (req, res) -> receivedEvents.add(Event.decodeEvent(req.getReader()).orElse(null)));
        Thread.sleep(100);
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(100);
        sourceServer.stop();
        localServer.stop();
    }

    @Test
    public void testReceieveEvent() throws Exception{
        Event event = EventBuilder.create().name("funny message")
                .type("funny reference").build();

        HttpPost post = new HttpPost(serverEventAddress);
        post.setEntity(new StringEntity(event.encodeEvent()));
        client.execute(post);
        Thread.sleep(20);

        List<Event> collectedEvents = eventCollector.findEventsWithId(event.getEventId());
        assertEquals("Wrong number of events recorded", 1, collectedEvents.size());
        assertEquals("Did not get the joke", "funny message", collectedEvents.get(0).getEventName());
        assertEquals("Did not get the reference", "funny reference", collectedEvents.get(0).getEventType());
    }

//    @Test
//    public void testGetServerId() throws Exception {
//        HttpGet request = new HttpGet("http://localhost:"+port+"/getServerId");
//
//        HttpResponse res = client.execute(request);
//        HttpEntity entity = res.getEntity();
//        if(entity != null) {
//            try (InputStream inputStream = entity.getContent()) {
//                String response = IOUtils.toString(inputStream, "UTF-8");
//                assertEquals("Did not recieve correct serverId", instance.getServerId().toString(), response);
//            }
//        }
//    }

    @Test
    public void testAddEventHandler() throws Exception {
        EventHandler handler = EventHandler.create().hasDataKey("TEST").build();
        Event createEvent = EventBuilder.create().name("Create Event Handler Event")
                .type("CREATE_EVENT_HANDLER")
                .data("serverId", instance.getServerId().toString())
                .data("eventHandlerDetails", handler.encodeToJson())
                .build();

        eventInjector.registerEvent(createEvent);
        // 2 default event handlers and one that we create
        assertEquals("Incorrect number of handlers present", 3, eventQueueInterface.getEventHandlers().size());
        Optional<EventHandler> rxHandlerOpt = eventQueueInterface.getEventHandlers().stream()
                .filter(h -> h.getEventHandlerId().equals(handler.getEventHandlerId())).findFirst();
        assertTrue("rxHandler is not present", rxHandlerOpt.isPresent());
        EventHandler rxHandler = rxHandlerOpt.get();

        Event testEvent = createMock(Event.class);
        expect(testEvent.getEventField("TEST")).andReturn("Some data").anyTimes();
        replayAll();
        assertEquals(true, handler.getHandlerCondition().test(testEvent));
        assertEquals(true, rxHandler.getHandlerCondition().test(testEvent));
        assertEquals(false, handler.getHandlerCondition().test(createEvent));
        assertEquals(false, rxHandler.getHandlerCondition().test(createEvent));
        verifyAll();
    }

    @Test
    public void testAddEventHandlerNetwork() throws Exception {
        EventHandler handler = EventHandler.create().hasDataKey("TEST")
                .httpEventReceiver(callbackAddress)
                .build();

        Event createEvent = EventBuilder.create().name("Create Event Handler Event")
                .type("CREATE_EVENT_HANDLER")
                .data("serverId", instance.getServerId().toString())
                .data("eventHandlerDetails", handler.encodeToJson())
                .build();

        HttpPost post = new HttpPost(serverEventAddress);
        post.setEntity(new StringEntity(createEvent.encodeEvent()));
        client.execute(post);

        // 2 default event handlers and one that we create
        assertEquals("Wrong number of event handlers present", 3, eventQueueInterface.getEventHandlers().size());
        Optional<EventHandler> rxHandlerOpt = eventQueueInterface.getEventHandlers().stream()
                .filter(h -> h.getEventHandlerId().equals(handler.getEventHandlerId())).findFirst();
        assertTrue(rxHandlerOpt.isPresent());
        EventHandler rxHandler = rxHandlerOpt.get();

        Event testEvent = createMock(Event.class);
        expect(testEvent.getEventField("TEST")).andReturn("Some data").anyTimes();

        Event testSentEvent = EventBuilder.create().name("funny reference").type("funny things")
                .data("TEST", "Some data").build();
        eventInjector.registerEvent(testSentEvent);
        Thread.sleep(50);

        replayAll();
        assertEquals(true, handler.getHandlerCondition().test(testEvent));
        assertEquals(true, rxHandler.getHandlerCondition().test(testEvent));
        assertEquals(false, handler.getHandlerCondition().test(createEvent));
        assertEquals(false, rxHandler.getHandlerCondition().test(createEvent));
        assertEquals(1, receivedEvents.size());
        assertEquals(testSentEvent.getEventId(), receivedEvents.get(0).getEventId());
        assertEquals("funny reference", receivedEvents.get(0).getEventName());
        assertEquals("funny things", receivedEvents.get(0).getEventType());
        verifyAll();
    }
}