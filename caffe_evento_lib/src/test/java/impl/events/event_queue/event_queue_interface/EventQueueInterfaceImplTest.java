package impl.events.event_queue.event_queue_interface;

import api.events.*;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.events.event_queue.event_queue_interface.EventQueueInterfaceChangedListener;
import impl.events.EventSourceImpl;
import impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.List;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Created by chris on 7/1/16.
 */
@RunWith( PowerMockRunner.class )
@PrepareForTest ( { EventQueueInterfaceChangedListener.class, EventSourceImpl.class, EventHandler.class } )
public class EventQueueInterfaceImplTest {
    EventQueueInterface instance = new EventQueueInterfaceImpl();

    @Mock
    EventQueueInterfaceChangedListener listener;
    @Mock
    EventSource eventSource;
    @Mock EventHandler eventHandler;

    @Before
    public void setUp() throws Exception {
        instance.addEventQueueInterfaceChangedListener(listener);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAddRemoveServiceChangedListener() {
        EventQueueInterfaceChangedListener mockListener = createMock(EventQueueInterfaceChangedListener.class);
        instance.addEventQueueInterfaceChangedListener(mockListener);
        List<EventQueueInterfaceChangedListener> listeners = Whitebox.getInternalState(instance, "eventQueueInterfaceChangedListeners");
        assertTrue(listeners.contains(mockListener));
        instance.removeEventQueueInterfaceChangedListener(mockListener);
        listeners = Whitebox.getInternalState(instance, "eventQueueInterfaceChangedListeners");
        assertFalse(listeners.contains(mockListener));
    }

    @Test
    public void testAddRemoveEventSource() {
        listener.addEventSource(eventSource);
        expectLastCall().once();

        listener.removeEventSource(eventSource);
        expectLastCall().once();

        replayAll();
        instance.addEventSource(eventSource);
        assertTrue(instance.getEventSources().contains(eventSource));
        instance.removeEventSource(eventSource);
        assertFalse(instance.getEventSources().contains(eventSource));
        verifyAll();
    }

    @Test
    public void testAddRemoveEventHandler() {
        listener.addEventHandler(eventHandler);
        expectLastCall().once();

        listener.removeEventHandler(eventHandler);
        expectLastCall().once();

        replayAll();
        instance.addEventHandler(eventHandler);
        assertTrue(instance.getEventHandlers().contains(eventHandler));
        instance.removeEventHandler(eventHandler);
        assertFalse(instance.getEventHandlers().contains(eventHandler));
        verifyAll();
    }

}