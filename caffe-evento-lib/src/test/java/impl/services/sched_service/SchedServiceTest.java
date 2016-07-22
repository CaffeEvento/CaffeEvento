package impl.services.sched_service;

import api.events.Event;
import api.events.event_queue.EventQueue;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.events.EventSource;
import api.utils.EventBuilder;
import impl.events.EventImpl;
import impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import impl.events.EventSourceImpl;
import impl.events.event_queue.SynchronousEventQueue;
import org.easymock.EasyMock;
import org.easymock.MockType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import test_util.EventCollector;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.Thread.sleep;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.junit.Assert.*;

/** TODO:Implement tests.
 * Created by eric on 7/15/16.
 */
@RunWith(PowerMockRunner.class)
public class SchedServiceTest {
    private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private ScheduledExecutorService executorService =
            //EasyMock.createMock(MockType.DEFAULT, ScheduledExecutorService.class);
            Executors.newScheduledThreadPool(1);
    private EventQueue eventQueue = new SynchronousEventQueue();
    private EventQueueInterface eventQueueInterface = new EventQueueInterfaceImpl();
    private SchedService instance;
    private EventCollector eventCollector = new EventCollector();
    private EventSource eventGenerator = new EventSourceImpl();

    @Before
    public void setUp() {
        eventQueue.addEventQueueInterface(eventQueueInterface);
        eventQueue.addEventHandler(eventCollector.getHandler());
        eventQueue.addEventSource(eventGenerator);
        instance = new SchedService(eventQueueInterface, clock, executorService);
    }

    //TODO: Clean up either testScheduleEvent so that it behaves consistently, or recode SchedService to provide more consistent behavior
    @Test
    public void testScheduleEvent() throws Exception {
        Event scheduledEvent = new EventImpl("Test Schedule Doer", "TestReq");

        //Clunky at best
        Map<String, String> params = new HashMap<>();
        params.put(impl.services.sched_service.SchedService.START_TIME, Date.from(Instant.now(clock).plus(200, MILLIS)).toString());
        Event schedulerEvent = impl.services.sched_service.SchedService.generateSchedulerEvent("Test Schedule", scheduledEvent, params);

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
        params.put(impl.services.sched_service.SchedService.DELAY, Duration.ZERO.plus(100, MILLIS).toString());
        params.put(impl.services.sched_service.SchedService.REPEAT_PERIOD, Duration.ZERO.plus(100, MILLIS).toString());
        params.put(impl.services.sched_service.SchedService.MAXDURATION, Duration.ZERO.plus(560, MILLIS).toString());
        Event schedulerEvent = impl.services.sched_service.SchedService.generateSchedulerEvent("Test Schedule", scheduledEvent, params);
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
        params.put(impl.services.sched_service.SchedService.DELAY, Duration.ZERO.plus(100, MILLIS).toString());
        Event schedulerEvent = impl.services.sched_service.SchedService.generateSchedulerEvent("Test Schedule", scheduledEvent, params);
        Event cancelEvent = impl.services.sched_service.SchedService.generateSchedulerCancelEvent("Test Schedule Cancel", UUID.fromString(schedulerEvent.getEventField(impl.services.sched_service.SchedService.SCHEDULE_ID_FIELD)));

        eventGenerator.registerEvent(schedulerEvent);
        assertEquals("unregistered scheduler too early", 1, instance.numberOfActiveSchedulers());
        assertEquals("registered scheduledEvent too early", 0, eventCollector.findEventsWithName("Test Schedule Doer").size());
        Thread.sleep(40);
        eventGenerator.registerEvent(cancelEvent);
        assertEquals("activeScheduler not removed", 0, instance.numberOfActiveSchedulers());
        assertEquals("Event fired when canceled1", 0, eventCollector.findEventsWithName("Test Schedule Doer").size());
        Thread.sleep(100);
        assertEquals("Event fired when canceled2", 0, eventCollector.findEventsWithName("Test Schedule Doer").size());
        assertEquals("Canceled Event did not fire", 1, eventCollector.findEventsWithType(impl.services.sched_service.SchedService.SCHEDULE_EVENT_CANCELED).size());
    }

    @Test
    // this test literally just checks to see if things break when supplying multiple scheduler events
    public void testMultipleSchedulerEvents() {
        Event scheduledEvent = new EventImpl("Test Schedule Doer", "TestReq");

        Map<String, String> params = new HashMap<>();
        params.put(impl.services.sched_service.SchedService.DELAY, Duration.ZERO.plus(200, MILLIS).toString());
        Event schedulerEvent = impl.services.sched_service.SchedService.generateSchedulerEvent("Test Schedule", scheduledEvent, params);
        for(int i = 0;i<200;i++) {
            eventGenerator.registerEvent(new EventImpl(schedulerEvent));
        }
    }
}