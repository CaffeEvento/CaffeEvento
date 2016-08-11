package com.cmbellis.caffeevento.lib.impl.services.echo_service;

import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.events.EventSource;
import com.cmbellis.caffeevento.lib.api.events.event_queue.EventQueue;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.api.utils.EventBuilder;
import com.cmbellis.caffeevento.lib.impl.events.EventSourceImpl;
import com.cmbellis.caffeevento.lib.impl.events.event_queue.SynchronousEventQueue;
import com.cmbellis.caffeevento.lib.impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import test_util.EventCollector;

import static com.cmbellis.caffeevento.lib.impl.services.echo_service.EchoService.createEchoEvent;
import static org.junit.Assert.*;

/**
 * Created by eric on 8/7/16.
 */
@RunWith(PowerMockRunner.class)
public class EchoServiceTest {
    private EventSource eventInjector = new EventSourceImpl();
    private EventCollector eventCollector = new EventCollector();
    private EventQueue eventQueue = new SynchronousEventQueue();
    private EventQueueInterface eventQueueInterface = new EventQueueInterfaceImpl();
    private EchoService instance = new EchoService(eventQueueInterface);

    @Before
    public void beforeEchoTest() {
        eventQueue.addEventQueueInterface(instance.getEventQueueInterface());
        eventQueue.addEventHandler(eventCollector.getHandler());
        eventQueue.addEventSource(eventInjector);
    }

    @Test
    public void testEcho() {
        Event message = EventBuilder.create()
                .name("This is a message")
                .type("Junk Mail")
                .build();
        createEchoEvent(message).send(eventInjector);
        assertEquals(2, eventCollector.getCollectedEvents().size());
        assertEquals(1, eventCollector.findEventsWithName(message.getEventName()).size());
        assertEquals(1, eventCollector.findEventsWithType(message.getEventType()).size());
    }

    @Test
    public void testEchoMessageMalformed() {
        EventBuilder.create()
                .name("This message will not Echo")
                .type("ECHO")
                .data("MESSAGE", "XD")
                .send(eventInjector);
        assertEquals(1,eventCollector.getCollectedEvents().size());
    }
}