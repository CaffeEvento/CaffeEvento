package impl.services.request_service;

import api.events.*;
import api.events.event_queue.EventQueue;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.utils.EventBuilder;
import impl.events.*;
import impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import impl.events.event_queue.SynchronousEventQueue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import test_util.EventCollector;


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
}
