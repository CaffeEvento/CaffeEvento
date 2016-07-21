package impl.events.event_queue;

import api.events.Event;
import api.events.EventHandler;
import api.events.event_queue.EventQueue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.Executor;

import static org.easymock.EasyMock.anyObject;
import static org.powermock.api.easymock.PowerMock.*;

/**
 * Created by chris on 7/21/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Executor.class, AsynchronousEventQueue.class, Event.class})
public class AsynchronousEventQueueTest {
    Executor executor;
    EventQueue instance;

    @Before
    public void setUp() {
        executor = createMock(Executor.class);
        instance = new AsynchronousEventQueue(executor);
    }

    @Test
    public void testExecutorSubmitsEvents() {
        int numHandlers = 3;
        for(int i = 0; i < numHandlers; i++) {
            instance.addEventHandler(createNiceMock(EventHandler.class));
        }

        executor.execute(anyObject(Runnable.class));
        expectLastCall().times(numHandlers);

        Event mockEvent = createMock(Event.class);

        replayAll();
        instance.receiveEvent(mockEvent);
        verifyAll();
    }
}