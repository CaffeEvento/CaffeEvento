package com.cmbellis.caffeevento.lib.impl.services.scheduler_service.schedulers;

import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.events.EventSource;
import com.cmbellis.caffeevento.lib.api.events.event_queue.EventQueue;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.api.utils.EventBuilder;
import com.cmbellis.caffeevento.lib.impl.events.EventSourceImpl;
import com.cmbellis.caffeevento.lib.impl.events.event_queue.SynchronousEventQueue;
import com.cmbellis.caffeevento.lib.impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import com.cmbellis.caffeevento.lib.impl.services.scheduler_service.Schedules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import test_util.EventCollector;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.cmbellis.caffeevento.lib.impl.services.scheduler_service.schedulers.AbstractScheduler.createScheduleCancelEvent;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Created by eric on 8/8/16.
 */
@RunWith(PowerMockRunner.class)
public class AbstractSchedulerTest {
    @Mock
    private Consumer<String> mockStart;
    @Mock
    private Consumer<String> mockStop;
    @Mock
    private Function<String, Boolean> mockValidator;

    private EventQueue eventQueue = new SynchronousEventQueue();
    private EventQueueInterface eventQueueInterface = new EventQueueInterfaceImpl();
    private EventCollector eventCollector = new EventCollector();
    private EventSource eventInjector = new EventSourceImpl();

    private static String format = "FORMAT";

    private AbstractScheduler instance = new AbstractScheduler(eventQueueInterface, format) {
        @Override
        protected Schedule scheduleJob(String args, String action, String id) {
            return new testSchedule(args, action, id);
        }

        @Override
        protected boolean validateArgs(String args) {
            return mockValidator.apply(args);
        }

        class testSchedule extends Schedule {
            protected void cancelJob() {
                mockStop.accept(id);
            }
            protected void startJob() {
                mockStart.accept(id);
            }
            testSchedule(String args, String action, String id) {
                super(id);
            }
        }
    };

    @Before
    public void setUp() throws Exception {
        eventQueue.registerService(instance);
        eventQueue.addEventHandler(eventCollector.getHandler());
        eventQueue.addEventSource(eventInjector);
    }

    @Test
    public void testAbstractScheduler() {
        // Things
        Event action = EventBuilder.create()
                .name("ActionEvent")
                .type("Test")
                .build();
        UUID SchedulerId = UUID.randomUUID();
        String args = "args";
        Event scheduleEvent = instance.createScheduleEvent(args, action, SchedulerId).build();
        Event cancelEvent = createScheduleCancelEvent(SchedulerId).build();

        // Expectations
        expect(mockValidator.apply(args)).andReturn(true).anyTimes();
        mockStart.accept(SchedulerId.toString());
        expectLastCall();
        mockStop.accept(SchedulerId.toString());
        expectLastCall();

        // Actions
        replayAll();
        eventInjector.registerEvent(scheduleEvent);
        assertEquals("wrong number of jobs", 1, instance.countActiveJobs());
        assertEquals("wrong number of events", 1, eventCollector.getCollectedEvents().size());
        eventInjector.registerEvent(cancelEvent);
        assertEquals("jobs not all canceled", 0, instance.countActiveJobs());
        assertEquals("wrong number of events after cancel", 3, eventCollector.getCollectedEvents().size());
        assertEquals("did not find unscheduled notification", 1, eventCollector.findEventsWithType(Schedules.UNSCHEDULED_ACTION).size());
        verifyAll();
    }
}