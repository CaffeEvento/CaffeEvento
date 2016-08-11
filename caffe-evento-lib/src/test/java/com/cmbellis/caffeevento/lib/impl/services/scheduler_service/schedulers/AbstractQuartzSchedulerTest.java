package com.cmbellis.caffeevento.lib.impl.services.scheduler_service.schedulers;

import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.events.EventSource;
import com.cmbellis.caffeevento.lib.api.events.event_queue.EventQueue;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.api.utils.EventBuilder;
import com.cmbellis.caffeevento.lib.impl.events.EventSourceImpl;
import com.cmbellis.caffeevento.lib.impl.events.event_queue.SynchronousEventQueue;
import com.cmbellis.caffeevento.lib.impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import test_util.EventCollector;

import java.util.UUID;
import java.util.function.Function;

import static java.lang.Thread.sleep;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.*;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by eric on 8/9/16.
 */
@RunWith(PowerMockRunner.class)
public class AbstractQuartzSchedulerTest {
    private EventQueue eventQueue = new SynchronousEventQueue();
    private EventCollector eventCollector = new EventCollector();
    private EventSource eventInjector = new EventSourceImpl();
    private EventQueueInterface eventQueueInterface = new EventQueueInterfaceImpl();
    private String format = "Format of Scheduler";

    @Mock
    private Scheduler scheduler;
    @Mock
    private Function<String, Trigger> mockTrigger;
    @Mock
    private Function<String, Boolean> mockValidate;

    private AbstractQuartzScheduler instance;

    @Before
    public void setUp() throws Exception {
        eventQueue.addEventSource(eventInjector);
        eventQueue.addEventHandler(eventCollector.getHandler());
        eventQueue.addEventQueueInterface(eventQueueInterface);
    }

    @Ignore
    @Test
    public void testQuartzScheduler() throws Exception{
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        instance = new AbstractQuartzScheduler(eventQueueInterface, format, scheduler) {
            @Override
            protected Trigger createTrigger(String args) {
                return mockTrigger.apply(args);
            }

            @Override
            protected boolean validateArgs(String args) {
                return mockValidate.apply(args);
            }
        };
        // Things
        Event action = EventBuilder.create()
                .name("Action")
                .type("generic")
                .build();
        String args = "args";
        UUID schedulerId = UUID.randomUUID();
        Event scheduleEvent = instance.createScheduleEvent(args, action, schedulerId)
                .name("Scheduling Example")
                .build();
        Event cancelEvent = instance.createScheduleCancelEvent(schedulerId)
                .build();
        Trigger theTrigger = newTrigger().startNow().build(); // according to the documentation this trigger should fire immediately
        // expectations
        expect(mockValidate.apply(args)).andReturn(true).anyTimes();
        expect(mockTrigger.apply(args)).andReturn(theTrigger).once();

        // actions
        assert(scheduler.isStarted());

        replayAll();
        eventInjector.registerEvent(scheduleEvent);

        assert(scheduler.checkExists(theTrigger.getKey()));
        assertEquals(1, eventCollector.findEventsWithName("Scheduling Example").size());
        assertEquals(1, instance.countActiveJobs());
        sleep(100);
        assertEquals(1, eventCollector.findEventsWithName("Action").size());
        // TODO: Get the quartz scheduler to actually register events
        eventInjector.registerEvent(cancelEvent);

        assertEquals(0, instance.countActiveJobs());
        assert(!scheduler.checkExists(theTrigger.getKey()));
        verifyAll();

        scheduler.shutdown();
        assert(scheduler.isShutdown());
    }
}