package services;

import event_queue.EventHandler;
import event_queue.EventSource;
import event_queue.service.Service;
import event_queue.service.ServiceChangedListener;
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
@PrepareForTest ( { ServiceChangedListener.class, EventSource.class, EventHandler.class } )
public class ServiceTest {
    MockService instance = new MockService();

    @Mock ServiceChangedListener listener;
    @Mock EventSource eventSource;
    @Mock EventHandler eventHandler;

    @Before
    public void setUp() throws Exception {
        instance = new MockService();
        instance.addServiceChangedListener(listener);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAddRemoveServiceChangedListener() {
        ServiceChangedListener mockListener = createMock(ServiceChangedListener.class);
        instance.addServiceChangedListener(mockListener);
        List<ServiceChangedListener> listeners = Whitebox.getInternalState(instance, "serviceChangedListeners");
        assertTrue(listeners.contains(mockListener));
        instance.removeServiceChangedListener(mockListener);
        listeners = Whitebox.getInternalState(instance, "serviceChangedListeners");
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


    private class MockService extends Service {
        public void addEventSource(EventSource theEventSource) {
            super.addEventSource(theEventSource);
        }

        public void removeEventSource(EventSource theEventSource) {
            super.removeEventSource(theEventSource);
        }

        public void addEventHandler(EventHandler theEventHandler) {
            super.addEventHandler(theEventHandler);
        }

        public void removeEventHandler(EventHandler theEventHandler) {
            super.removeEventHandler(theEventHandler);
        }
    }

}