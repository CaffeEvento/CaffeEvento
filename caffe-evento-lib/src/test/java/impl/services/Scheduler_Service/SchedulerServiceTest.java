package impl.services.Scheduler_Service;

import api.events.Event;
import api.events.event_queue.EventQueue;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by eric on 7/27/16.
 */
public class SchedulerServiceTest {
    @Mock private Event event;
    @Mock private EventQueue eventQueue;

    private SchedulerService instance;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testScheduleEvent() {
        
    }
}