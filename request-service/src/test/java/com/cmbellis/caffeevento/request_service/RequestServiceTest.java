package com.cmbellis.caffeevento.request_service;

import com.cmbellis.caffeevento.lib.api.events.*;
import com.cmbellis.caffeevento.lib.api.events.event_queue.EventQueue;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.impl.events.*;
import com.cmbellis.caffeevento.lib.impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import com.cmbellis.caffeevento.lib.impl.events.event_queue.SynchronousEventQueue;
import com.cmbellis.caffeevento.request_service.impl.request_service.RequestService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import com.cmbellis.caffeevento.lib.test_util.EventCollector;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by chris on 7/9/16.
 */
@RunWith(PowerMockRunner.class)
public class RequestServiceTest {
    private EventQueue eventQueue = new SynchronousEventQueue();
    private EventQueueInterface eventQueueInterface = new EventQueueInterfaceImpl();
    private RequestService instance = new RequestService(eventQueueInterface);
    private EventCollector eventCollector = new EventCollector();
    private EventSource eventGenerator = new EventSourceImpl();

    @Before
    public void setUp() {
        eventQueue.addEventQueueInterface(eventQueueInterface);
        eventQueue.addEventHandler(eventCollector.getHandler());
        eventQueue.addEventSource(eventGenerator);
    }

    @Test
    public void testInitialRequest() {
        Event fufillerEvent = new EventImpl("Test Request Doer", "TestReq");
        RequestService.generateRequestEvent("Test Request", fufillerEvent).send(eventGenerator);
        assertEquals(1, eventCollector.findEventsWithName("Test Request Doer").size());
        assertEquals(1, instance.numberOfActiveRequests());
    }

    @Test
    public void testRequestFufilled() {
        Event fufillerEvent = new EventImpl("Test Request Doer", "TestReq");
        Event requestEvent = RequestService.generateRequestEvent("Test Request", fufillerEvent).build();
        UUID requestId = UUID.fromString(requestEvent.getEventField(RequestService.REQUEST_ID_FIELD));
        eventGenerator.registerEvent(requestEvent);
        assertEquals(1, instance.numberOfActiveRequests());
        RequestService.generateRequestSuccessEvent("Request Success!", requestId).send(eventGenerator);
        assertEquals(0, instance.numberOfActiveRequests());
        assertEquals(1, eventCollector.findEventsWithType(RequestService.REQUEST_COMPLETED_EVENT).size());
    }

    @Test
    public void testRequestFailed() {
        Event fufillerEvent = new EventImpl("Test Request Doer", "TestReq");
        Event requestEvent = RequestService.generateRequestEvent("Test Request", fufillerEvent).build();
        UUID requestId = UUID.fromString(requestEvent.getEventField(RequestService.REQUEST_ID_FIELD));
        eventGenerator.registerEvent(requestEvent);
        assertEquals(1, instance.numberOfActiveRequests());
        RequestService.generateRequestFailedEvent("Request Failed :-(", requestId).send(eventGenerator);
        assertEquals(1, instance.numberOfActiveRequests());
        assertEquals(2, eventCollector.findEventsWithName("Test Request Doer").size());
    }

    @Test
    public void testFailuresMoreThanMax() {
        Event fufillerEvent = new EventImpl("Test Request Doer", "TestReq");
        Event requestEvent = RequestService.generateRequestEvent("Test Request", fufillerEvent).build();
        UUID requestId = UUID.fromString(requestEvent.getEventField(RequestService.REQUEST_ID_FIELD));
        eventGenerator.registerEvent(requestEvent);
        assertEquals(1, instance.numberOfActiveRequests());
        for(int i = 0; i < RequestService.MAX_RETRIES; i++){
            assertEquals(i + 1, eventCollector.findEventsWithName("Test Request Doer").size());
            RequestService.generateRequestFailedEvent("Request Failed :-(", requestId).send(eventGenerator);
            assertEquals(1, instance.numberOfActiveRequests());
            assertEquals(i + 2, eventCollector.findEventsWithName("Test Request Doer").size());
        }
        RequestService.generateRequestFailedEvent("Final Request Failed :-(", requestId).send(eventGenerator);
        assertEquals(0, instance.numberOfActiveRequests());
    }

    @Test
    public void testFailuresConfiguredLessThanDefault(){
        testFailuresConfiguredToAmount(RequestService.MAX_RETRIES - 2);
    }

    @Test
    public void testFailuresConfiguredMoreThanDefault(){
        testFailuresConfiguredToAmount(RequestService.MAX_RETRIES + 2);
    }

    private void testFailuresConfiguredToAmount(int amount){
        Event fufillerEvent = new EventImpl("Test Request Doer", "TestReq");
        Event requestEvent = RequestService.generateRequestEvent("Test Request", fufillerEvent)
                .data(RequestService.REQUEST_MAX_RETRIES_FIELD, String.valueOf(amount))
                .build();
        UUID requestId = UUID.fromString(requestEvent.getEventField(RequestService.REQUEST_ID_FIELD));
        eventGenerator.registerEvent(requestEvent);
        assertEquals(1, instance.numberOfActiveRequests());
        for(int i = 0; i < amount; i++){
            assertEquals(i + 1, eventCollector.findEventsWithName("Test Request Doer").size());
            RequestService.generateRequestFailedEvent("Request Failed :-(", requestId).send(eventGenerator);
            assertEquals(1, instance.numberOfActiveRequests());
            assertEquals(i + 2, eventCollector.findEventsWithName("Test Request Doer").size());
        }
        RequestService.generateRequestFailedEvent("Final Request Failed :-(", requestId).send(eventGenerator);
        assertEquals(0, instance.numberOfActiveRequests());
    }

}