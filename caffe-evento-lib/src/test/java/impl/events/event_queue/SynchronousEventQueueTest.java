package impl.events.event_queue;

import api.events.event_queue.EventQueue;
import com.google.common.collect.Lists;
import api.events.*;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import impl.events.EventSourceImpl;
import impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import impl.events.event_queue.SynchronousEventQueue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Created by chris on 7/1/16.
 */
@RunWith( PowerMockRunner.class )
@PrepareForTest( { EventQueueInterfaceImpl.class } )
public class SynchronousEventQueueTest {
    private EventQueue instance = new SynchronousEventQueue();

    @Mock
    private Event event;

    @Mock
    private EventQueueInterface eventQueueInterface;

    private List<EventHandler> eventHandlers;
    private List<EventSource> eventSources;

    @Before
    public void setUp() {
        eventHandlers = Lists.newArrayList(createMock(EventHandler.class), createMock(EventHandler.class));
        eventSources = Lists.newArrayList(PowerMock.createMock(EventSourceImpl.class), createMock(EventSourceImpl.class));
    }

    private void prepareService(EventQueueInterface theEventQueueInterface) {
        expect(theEventQueueInterface.getEventHandlers()).andReturn(eventHandlers).once();
        expect(theEventQueueInterface.getEventSources()).andReturn(eventSources).once();

        theEventQueueInterface.addEventQueueInterfaceChangedListener(instance);
        expectLastCall().once();

        eventSources.forEach(source -> {
            source.addListener(instance);
            expectLastCall().once();
        });
    }

    @Test
    public void testReceiveEvent() {
        prepareService(eventQueueInterface);

        eventHandlers.forEach(eventHandler -> {
            eventHandler.handleEvent(event);
            expectLastCall().once();

            expect(eventHandler.getHandlerCondition()).andReturn(event -> true);
        });

        replayAll();
        instance.addEventQueueInterface(eventQueueInterface);
        instance.receiveEvent(event);
        verifyAll();
    }

    @Test
    public void testPredicateFalse() {
        prepareService(eventQueueInterface);

        eventHandlers.forEach(eventHandler -> {
            expect(eventHandler.getHandlerCondition()).andReturn(event -> false);
        });

        replayAll();
        instance.addEventQueueInterface(eventQueueInterface);
        instance.receiveEvent(event);
        verifyAll();
    }

    @Test
    public void testRegisterUnregisterService() {
        expect(eventQueueInterface.getEventHandlers()).andReturn(eventHandlers).times(2);
        expect(eventQueueInterface.getEventSources()).andReturn(eventSources).times(2);

        eventSources.forEach(source -> {
            source.addListener(instance);
            expectLastCall().once();

            source.removeListener(instance);
            expectLastCall().once();
        });

        eventQueueInterface.addEventQueueInterfaceChangedListener(instance);
        expectLastCall().once();

        eventQueueInterface.removeEventQueueInterfaceChangedListener(instance);
        expectLastCall().once();

        replayAll();
        instance.addEventQueueInterface(eventQueueInterface);
        List<EventQueueInterface> eventQueueInterfaces = Whitebox.getInternalState(instance, "eventQueueInterfaces");
        List<EventHandler> handlers = Whitebox.getInternalState(instance, "eventHandlers");
        List<EventSource> sources = Whitebox.getInternalState(instance, "eventSources");
        assertTrue(eventQueueInterfaces.contains(eventQueueInterface));
        assertTrue(handlers.containsAll(eventHandlers));
        assertTrue(sources.containsAll(eventSources));

        instance.removeEventQueueInterface(eventQueueInterface);
        eventQueueInterfaces = Whitebox.getInternalState(instance, "eventQueueInterfaces");
        handlers = Whitebox.getInternalState(instance, "eventHandlers");
        sources = Whitebox.getInternalState(instance, "eventSources");
        assertFalse(eventQueueInterfaces.contains(eventQueueInterface));
        assertFalse(handlers.containsAll(eventHandlers));
        assertFalse(sources.containsAll(eventSources));

        verifyAll();
    }



}