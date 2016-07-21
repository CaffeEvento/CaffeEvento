package impl.services.scheduler_service;

import api.events.Event;
import api.events.EventSource;
import api.events.event_queue.EventQueue;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import impl.events.EventImpl;
import impl.events.EventSourceImpl;
import impl.events.event_queue.SynchronousEventQueue;
import impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import test_util.EventCollector;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by eric on 7/21/16.
 */
@RunWith(PowerMockRunner.class)
public class SchedulerServiceMalformattedTest {
    private EventQueue eventQueue = new SynchronousEventQueue();
    private EventQueueInterface eventQueueInterface = new EventQueueInterfaceImpl();
    private SchedulerService instance = new SchedulerService(eventQueueInterface);
    private EventCollector eventCollector = new EventCollector();
    private EventSource eventGenerator = new EventSourceImpl();

    @Before
    public void setUp() {
        eventQueue.addEventQueueInterface(eventQueueInterface);
        eventQueue.addEventHandler(eventCollector.getHandler());
        eventQueue.addEventSource(eventGenerator);
    }

    /* Mandatory Fields */
    @Test
    //This test intentionally sends a badly formatted field to the Scheduler in order to force logging
    public void testLogBadActionEvent() throws Exception {
        Event scheduledEvent = new EventImpl("Test Schedule Doer", "TestReq");

        //Clunky at best
        Map<String, String> params = new HashMap<>();
        Event schedulerEvent = SchedulerService.generateSchedulerEvent("Test Schedule", scheduledEvent, params);
        // Intentionally malformat the JSON
        schedulerEvent.setEventField(SchedulerService.SCHEDULED_EVENT_ACTION, "XD");
        eventGenerator.registerEvent(schedulerEvent);
    }

    @Test
    //This test intentionally sends a badly formatted field to the Scheduler in order to force logging
    public void testLogBadSchedulerIdEvent() throws Exception {
        Event scheduledEvent = new EventImpl("Test Schedule Doer", "TestReq");

        //Clunky at best
        Map<String, String> params = new HashMap<>();
        Event schedulerEvent = SchedulerService.generateSchedulerEvent("Test Schedule", scheduledEvent, params);
        // Intentionally malformat the UUID
        schedulerEvent.setEventField(SchedulerService.SCHEDULE_ID_FIELD, "XD");
        eventGenerator.registerEvent(schedulerEvent);
    }

    /* Optional Fields */
    @Test
    //This test intentionally sends a badly formatted field to the Scheduler in order to force logging
    public void testLogBadDelayEvent() throws Exception {
        Event scheduledEvent = new EventImpl("Test Schedule Doer", "TestReq");

        //Clunky at best
        Map<String, String> params = new HashMap<>();
        params.put(SchedulerService.DELAY, "XD");
        Event schedulerEvent = SchedulerService.generateSchedulerEvent("Test Schedule", scheduledEvent, params);
        eventGenerator.registerEvent(schedulerEvent);
    }

    @Test
    //This test intentionally sends a badly formatted field to the Scheduler in order to force logging
    public void testLogBadStartEvent() throws Exception {
        Event scheduledEvent = new EventImpl("Test Schedule Doer", "TestReq");

        //Clunky at best
        Map<String, String> params = new HashMap<>();
        params.put(SchedulerService.START_TIME, "XD");
        Event schedulerEvent = SchedulerService.generateSchedulerEvent("Test Schedule", scheduledEvent, params);
        eventGenerator.registerEvent(schedulerEvent);
    }

    @Test
    //This test intentionally sends a badly formatted field to the Scheduler in order to force logging
    public void testLogBadMaxDurationEvent() throws Exception {
        Event scheduledEvent = new EventImpl("Test Schedule Doer", "TestReq");

        //Clunky at best
        Map<String, String> params = new HashMap<>();
        params.put(SchedulerService.MAXDURATION, "XD");
        Event schedulerEvent = SchedulerService.generateSchedulerEvent("Test Schedule", scheduledEvent, params);
        eventGenerator.registerEvent(schedulerEvent);
    }

    @Test
    //This test intentionally sends a badly formatted field to the Scheduler in order to force logging
    public void testLogBadEndTimeEvent() throws Exception {
        Event scheduledEvent = new EventImpl("Test Schedule Doer", "TestReq");

        //Clunky at best
        Map<String, String> params = new HashMap<>();
        params.put(SchedulerService.END_TIME, "XD");
        Event schedulerEvent = SchedulerService.generateSchedulerEvent("Test Schedule", scheduledEvent, params);
        eventGenerator.registerEvent(schedulerEvent);
    }

    @Test
    //This test intentionally sends a badly formatted field to the Scheduler in order to force logging
    public void testLogBadPeriodEvent() throws Exception {
        Event scheduledEvent = new EventImpl("Test Schedule Doer", "TestReq");

        //Clunky at best
        Map<String, String> params = new HashMap<>();
        params.put(SchedulerService.REPEAT_PERIOD, "XD");
        Event schedulerEvent = SchedulerService.generateSchedulerEvent("Test Schedule", scheduledEvent, params);
        eventGenerator.registerEvent(schedulerEvent);
    }
}
