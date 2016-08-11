package com.cmbellis.caffeevento.lib.impl.services.container_service;

import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.events.EventHandler;
import com.cmbellis.caffeevento.lib.api.events.EventSource;
import com.cmbellis.caffeevento.lib.api.events.event_queue.EventQueue;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.api.utils.EventBuilder;
import com.cmbellis.caffeevento.lib.impl.events.EventHandlerImpl;
import com.cmbellis.caffeevento.lib.impl.events.EventSourceImpl;
import com.cmbellis.caffeevento.lib.impl.events.event_queue.SynchronousEventQueue;
import com.cmbellis.caffeevento.lib.impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import test_util.EventCollector;

import java.util.ArrayList;
import java.util.List;

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

    @Ignore
    @Test
    public void testServiceContainerEventQueueDoubleRegistration() {
        ServiceContainerEventQueue instance = new ServiceContainerEventQueue(eventQueueInterface, SynchronousEventQueue::new) {
            @Override
            protected void elevate(Event event) {
                EventBuilder.create()
                        .name("Elevated Event")
                        .type("Upgrade")
                        .data("Event", event.encodeEvent())
                        .send(elevateGenerator);
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
        assertEquals(eventCollector.getCollectedEvents().size(), 1, "External Events");
        assertEquals(eventCollector1.findEventsWithName("Injected Event").size(), 1, "Internal Events");
        EventBuilder.create()
                .name("Injected Event")
                .type("Another Single Event")
                .data("DX","Valid")
                .send(eventInjector);
        assertEquals(eventCollector.getCollectedEvents().size(), 2, "External Events");
        assertEquals(eventCollector1.findEventsWithName("Injected Event").size(), 2, "Internal Events");
        EventBuilder.create()
                .name("Injected Event")
                .type("Duplicating Event")
                .data("XD","Invalid")
                .data("DX","Invalid")
                .send(eventInjector);
        assertEquals(eventCollector.getCollectedEvents().size(), 3, "External Events");
        assertEquals(eventCollector1.findEventsWithName("Injected Event").size(), 3, "Internal Events");
        EventBuilder.create()
                .name("Unrecieved Event")
                .type("Non-event")
                .send(eventInjector);
        assertEquals(eventCollector.getCollectedEvents().size(), 4, "External Events");
        assertEquals(eventCollector1.findEventsWithName("Injected Event").size(), 3, "Internal Events");
        assertEquals(eventCollector1.getCollectedEvents().size(), 3, "Internal Events no filter");
    }
}