package com.cmbellis.caffeevento.request_service;

import com.cmbellis.caffeevento.lib.api.events.*;
import com.cmbellis.caffeevento.lib.api.events.event_queue.EventQueue;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.api.utils.EventBuilder;
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
 * Created by eric on 7/21/16.
 */
@RunWith(PowerMockRunner.class)
public class RequestServiceMalformattedTest {
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
    public void testMalformattedRequestFufillmentEvent() {
        RequestService.generateRequestEvent("Bad Fufiller Request", EventBuilder.create().build())
                .data(RequestService.REQUEST_EVENT_FUFILLMENT,"XD")
                .send(eventGenerator);
    }

    @Test
    public void testMalformattedRequestIdEvent() {
        RequestService.generateRequestEvent("Bad requestId Request", EventBuilder.create().build())
                .data(RequestService.REQUEST_ID_FIELD, "XD")
                .send(eventGenerator);
    }

    @Test
    public void testFailuresConfiguredToInvalid(){
        Event fufillerEvent = new EventImpl("Test Request Doer", "TestReq");
        Event requestEvent = RequestService.generateRequestEvent("Test Request", fufillerEvent)
                .data(RequestService.REQUEST_MAX_RETRIES_FIELD, "XD")
                .build();
        UUID requestId = UUID.fromString(requestEvent.getEventField(RequestService.REQUEST_ID_FIELD));
        eventGenerator.registerEvent(requestEvent);
        assertEquals(1, instance.numberOfActiveRequests());
        Assert.assertEquals(1, eventCollector.findEventsWithName("Test Request Doer").size());

        RequestService.generateRequestFailedEvent("Final Request Failed :-(", requestId).send(eventGenerator);

        assertEquals(0, instance.numberOfActiveRequests());
        Assert.assertEquals(1, eventCollector.findEventsWithName("Test Request Doer").size());
    }
}
