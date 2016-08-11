package com.cmbellis.caffeevento.lib.impl.events;

import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.events.event_queue.EventQueue;
import com.cmbellis.caffeevento.lib.api.events.EventSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.EasyMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Created by chris on 7/2/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { EventQueue.class, EventImpl.class })
public class EventSourceImplTest {
    private EventSource instance = new EventSourceImpl();

    @Mock
    private Event event;
    @Mock
    private EventQueue eventSink;

    @Before
    public void setUp() throws Exception {
        instance.addListener(eventSink);
    }

    @Test
    public void testListenersReceiveEvents() {
        eventSink.receiveEvent(event);
        expectLastCall().once();

        replayAll();
        instance.registerEvent(event);
        verifyAll();
    }

    @Test
    public void testRegisterUnregisterNewListeners() {
        eventSink.receiveEvent(event);
        expectLastCall().times(3);
        EventQueue eventListener = createMock(EventQueue.class);
        eventListener.receiveEvent(event);
        expectLastCall().once();

        replayAll();
        instance.registerEvent(event);
        instance.addListener(eventListener);
        instance.registerEvent(event);
        instance.removeListener(eventListener);
        instance.registerEvent(event);
        verifyAll();
    }
}