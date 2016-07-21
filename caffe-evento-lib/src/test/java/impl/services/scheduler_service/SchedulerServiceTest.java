package impl.services.scheduler_service;

import api.events.Event;
import api.events.event_queue.EventQueue;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.events.EventSource;
import api.utils.EventBuilder;
import impl.events.EventImpl;
import impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import impl.events.EventSourceImpl;
import impl.events.event_queue.SynchronousEventQueue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import test_util.EventCollector;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.lang.Thread.sleep;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.junit.Assert.*;

/** TODO:Implement tests.
 * Created by eric on 7/15/16.
 */
@RunWith(PowerMockRunner.class)
public class SchedulerServiceTest {
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

    //TODO: Clean up either testScheduleEvent so that it behaves consistently, or recode SchedulerService to provide more consistent behavior
    @Test
    public void testScheduleEvent() throws Exception {
        Event scheduledEvent = new EventImpl("Test Schedule Doer", "TestReq");

        //Clunky at best
        Map<String, String> params = new HashMap<>();
        params.put(SchedulerService.START_TIME, Date.from(Instant.now().plus(200, MILLIS)).toString());
        Event schedulerEvent = SchedulerService.generateSchedulerEvent("Test Schedule", scheduledEvent, params);

        eventGenerator.registerEvent(schedulerEvent);
        assertEquals("unregistered scheduler too early", 1, instance.numberOfActiveSchedulers());
        assertEquals("registered scheduledEvent too early", 0, eventCollector.findEventsWithName("Test Schedule Doer").size());
        sleep(250);
        assertEquals("activeScheduler not removed", 0, instance.numberOfActiveSchedulers());
        assertEquals(1, eventCollector.findEventsWithName("Test Schedule Doer").size());
    }

    @Test
    public void testScheduleRepeatingEvent() throws Exception {
        Event scheduledEvent = EventBuilder.create()
                .name("Test Schedule Doer")
                .type("TestReq")
                .build();
        Map<String, String> params = new HashMap<>();
        params.put(SchedulerService.DELAY, Duration.ZERO.plus(100, MILLIS).toString());
        params.put(SchedulerService.REPEAT_PERIOD, Duration.ZERO.plus(100, MILLIS).toString());
        params.put(SchedulerService.MAXDURATION, Duration.ZERO.plus(560, MILLIS).toString());
        Event schedulerEvent = SchedulerService.generateSchedulerEvent("Test Schedule", scheduledEvent, params);
        eventGenerator.registerEvent(schedulerEvent);
        assertEquals("unregistered scheduler too early", 1, instance.numberOfActiveSchedulers());
        assertEquals("registered scheduledEvent too early", 0, eventCollector.findEventsWithName("Test Schedule Doer").size());
        sleep(110);
        assertEquals("Did not register any events", true, eventCollector.findEventsWithName("Test Schedule Doer").size() > 0);
        sleep(600);
        assertEquals("activeScheduler not removed", 0, instance.numberOfActiveSchedulers());
        assertEquals("Wrong number of events fired", 6, eventCollector.findEventsWithName("Test Schedule Doer").size());
    }

    @Test
    public void testCancelEvent() throws Exception {
        Event scheduledEvent = new EventImpl("Test Schedule Doer", "TestReq");

        //Clunky at best
        Map<String, String> params = new HashMap<>();
        params.put(SchedulerService.DELAY, Duration.ZERO.plus(100, MILLIS).toString());
        Event schedulerEvent = SchedulerService.generateSchedulerEvent("Test Schedule", scheduledEvent, params);
        Event cancelEvent = SchedulerService.generateSchedulerCancelEvent("Test Schedule Cancel", UUID.fromString(schedulerEvent.getEventField(SchedulerService.SCHEDULE_ID_FIELD)));

        eventGenerator.registerEvent(schedulerEvent);
        assertEquals("unregistered scheduler too early", 1, instance.numberOfActiveSchedulers());
        assertEquals("registered scheduledEvent too early", 0, eventCollector.findEventsWithName("Test Schedule Doer").size());
        Thread.sleep(40);
        eventGenerator.registerEvent(cancelEvent);
        assertEquals("activeScheduler not removed", 0, instance.numberOfActiveSchedulers());
        assertEquals("Event fired when canceled1", 0, eventCollector.findEventsWithName("Test Schedule Doer").size());
        Thread.sleep(100);
        assertEquals("Event fired when canceled2", 0, eventCollector.findEventsWithName("Test Schedule Doer").size());
        assertEquals("Canceled Event did not fire", 1, eventCollector.findEventsWithType(SchedulerService.SCHEDULE_EVENT_CANCELED).size());
    }
}