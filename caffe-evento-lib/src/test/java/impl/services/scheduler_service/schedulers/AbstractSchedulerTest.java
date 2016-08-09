package impl.services.scheduler_service.schedulers;

import api.events.Event;
import api.events.EventSource;
import api.events.event_queue.EventQueue;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.utils.EventBuilder;
import impl.events.EventSourceImpl;
import impl.events.event_queue.SynchronousEventQueue;
import impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.quartz.*;
import test_util.EventCollector;

import java.util.UUID;
import java.util.function.Function;

import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Created by eric on 8/8/16.
 */
@RunWith(PowerMockRunner.class)
public class AbstractSchedulerTest {
    @Mock
    private Scheduler mockScheduler;
    @Mock
    private Trigger mockTrigger;

    private EventQueue eventQueue = new SynchronousEventQueue();
    private EventQueueInterface eventQueueInterface = new EventQueueInterfaceImpl();
    private EventCollector eventCollector = new EventCollector();
    private EventSource eventInjector = new EventSourceImpl();

    private static String format = "FORMAT";

    private AbstractScheduler instance = new AbstractScheduler(eventQueueInterface, format, mockScheduler) {
        @Override
        protected Trigger createTrigger(String args) {
            return mockTrigger;
        }

        @Override
        protected boolean validateArgs(String args) {
            return true;
        }
    };

    @Before
    public void setUp() throws Exception {
        eventQueue.registerService(instance);
        eventQueue.addEventHandler(eventCollector.getHandler());
        eventQueue.addEventSource(eventInjector);
    }

    @Ignore
    @Test
    public void testAbstractScheduler() {
        Event action = EventBuilder.create()
                .name("ActionEvent")
                .type("Test")
                .build();
        UUID SchedulerId = UUID.randomUUID();
        String args = "args";
        Event scheduleEvent = instance.createScheduleEvent(args, action, SchedulerId).build();

        replayAll();
        eventInjector.registerEvent(scheduleEvent);
        verifyAll();
    }

    @Ignore
    @Test
    public void countActiveJobs() {

    }

}