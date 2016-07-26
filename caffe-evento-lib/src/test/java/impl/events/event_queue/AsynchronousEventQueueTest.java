package impl.events.event_queue;

import api.events.Event;
import api.events.EventHandler;
import api.events.event_queue.EventQueue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.*;
import java.util.function.Consumer;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.mock;
import static org.powermock.api.easymock.PowerMock.*;

/**
 * Created by chris on 7/21/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Executor.class, AsynchronousEventQueue.class, Event.class, Consumer.class})
public class AsynchronousEventQueueTest {
    @Mock ExecutorService executor;
    @Mock Consumer<Event> defaultHandler;
    @Mock Event event;

    EventQueue instance;

    @Before
    public void setUp() {
        instance = new AsynchronousEventQueue(executor, defaultHandler);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEventHandlersHandleEvents() throws Exception {
        int numHandlers = 3;
        for(int i = 0; i < numHandlers; i++) {
            instance.addEventHandler(createMock(EventHandler.class));
        }

        Future<Boolean> mockFuture = (Future<Boolean>)createMock(Future.class);
        expect(mockFuture.get()).andReturn(true).anyTimes();
        expect(executor.submit((Callable<Boolean>)anyObject(Callable.class))).andReturn(mockFuture).times(numHandlers);

        replayAll();
        instance.receiveEvent(event);
        verifyAll();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDefaultHandlerWithHandlers() throws Exception {
        int numHandlers = 3;
        for(int i = 0; i < numHandlers; i++) {
            instance.addEventHandler(createMock(EventHandler.class));
        }

        Future<Boolean> mockFuture = (Future<Boolean>)createMock(Future.class);
        expect(mockFuture.get()).andReturn(false).anyTimes();
        expect(executor.submit((Callable<Boolean>)anyObject(Callable.class))).andReturn(mockFuture).times(numHandlers);

        defaultHandler.accept(event);
        expectLastCall();

        replayAll();
        instance.receiveEvent(event);
        verifyAll();
    }

    @Test
    public void testDefaultHandler() {
        defaultHandler.accept(event);
        expectLastCall();

        replayAll();
        instance.receiveEvent(event);
        verifyAll();
    }
}