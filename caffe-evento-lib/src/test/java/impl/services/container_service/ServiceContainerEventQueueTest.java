package impl.services.container_service;

import api.events.Event;
import api.events.EventHandler;
import api.events.EventSource;
import api.events.event_queue.EventQueue;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.utils.EventBuilder;
import impl.events.EventHandlerImpl;
import impl.events.EventSourceImpl;
import impl.events.event_queue.SynchronousEventQueue;
import impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.annotations.BeforeMethod;
import test_util.EventCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

/**
 * Created by eric on 8/7/16.
 */
@RunWith(PowerMockRunner.class)
public class ServiceContainerEventQueueTest {
    private EventQueueInterface eventQueueInterface = new EventQueueInterfaceImpl();
    private EventQueue eventQueue = new SynchronousEventQueue();
    private EventSource eventInjector = new EventSourceImpl();
    private EventCollector eventCollector = new EventCollector();
    private EventCollector eventCollector1 = new EventCollector();

    @Before
    public void setUp() throws Exception {
        eventQueue.addEventHandler(eventCollector.getHandler());
        eventQueue.addEventSource(eventInjector);
    }

    @Test
    public void testServiceContainerEventQueueDoubleRegistration() {
        ServiceContainerEventQueue instance = new ServiceContainerEventQueue(eventQueueInterface, SynchronousEventQueue::new) {
            @Override
            protected void elevate(Event event) {
                EventBuilder.create().name("Elevated Event").type("Upgrade").data("Event", event.encodeEvent()).send(elevateGenerator);
            }

            @Override
            protected List<EventHandlerImpl.EventHandlerBuilder> pullCriteria() {
                List<EventHandlerImpl.EventHandlerBuilder> pullHandlers = new ArrayList<>();
                pullHandlers.add(EventHandler.create().hasDataKey("XD"));
                pullHandlers.add(EventHandler.create().hasDataKey("DX"));
                return pullHandlers;
            }
        };
        eventQueue.registerService(instance);
        instance.addEventHandler(eventCollector1.getHandler());

        EventBuilder.create()
                .name("Injected Event")
                .type("Single Event")
                .data("XD","Valid")
                .send(eventInjector);
        EventBuilder.create()
                .name("Injected Event")
                .type("Another Single Event")
                .data("DX","Valid")
                .send(eventInjector);
        EventBuilder.create()
                .name("Injected Event")
                .type("Duplicating Event")
                .data("XD","Invalid")
                .data("DX","Invalid")
                .send(eventInjector);

        assertEquals(eventCollector.getCollectedEvents().size(), 3, "External Events");
        assertEquals(eventCollector1.findEventsWithName("Injected Event").size(), 3, "Internal Events");
    }
}