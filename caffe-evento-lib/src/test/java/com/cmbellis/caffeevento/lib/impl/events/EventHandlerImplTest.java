package com.cmbellis.caffeevento.lib.impl.events;

import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.events.EventHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.function.Consumer;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.*;

/**
 * Created by chris on 7/13/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Event.class, EventHandlerImpl.class})
public class EventHandlerImplTest {

    @Mock
    Event event;

    // We want to make the assertion that everything that works on a local handler will work on a remote
    // handler, so no trickery can be made, I wrote this to execute an event handler on both the local
    // and the remote in one fell swoop
    public void executeHandlerTest(EventHandler original, Consumer<EventHandler> handlerTest) {
        EventHandler copy = EventHandler.fromJson(original.encodeToJson()).orElse(null);

        handlerTest.accept(original);
        handlerTest.accept(copy);
    }

    @Test
    public void testEventHandlerEventData() {
        EventHandler handler = EventHandler.create().eventData("TEST_KEY", "TEST_VALUE").build();
        expect(event.getEventField("TEST_KEY")).andReturn("TEST_VALUE").anyTimes();

        replayAll();
        executeHandlerTest(handler, h -> assertEquals(true, h.getHandlerCondition().test(event)));
        verifyAll();
    }

    @Test
    public void testMultiEventHandlerEventDataPass() {
        EventHandler handler = EventHandler.create()
                .eventData("TEST_KEY1", "TEST_VALUE1")
                .eventData("TEST_KEY2", "TEST_VALUE2").build();
        expect(event.getEventField("TEST_KEY1")).andReturn("TEST_VALUE1").anyTimes();
        expect(event.getEventField("TEST_KEY2")).andReturn("TEST_VALUE2").anyTimes();

        replayAll();
        executeHandlerTest(handler, h -> assertEquals(true, h.getHandlerCondition().test(event)));
        verifyAll();
    }

    @Test
    public void testMultiEventHandlerEventDataFail() {
        EventHandler handler = EventHandler.create()
                .eventData("TEST_KEY1", "TEST_VALUE1")
                .eventData("TEST_KEY2", "TEST_VALUE").build();
        expect(event.getEventField("TEST_KEY1")).andReturn("TEST_VALUE1").anyTimes();
        expect(event.getEventField("TEST_KEY2")).andReturn("TEST_VALUE2").anyTimes();

        replayAll();
        executeHandlerTest(handler, h -> assertEquals(false, h.getHandlerCondition().test(event)));
        verifyAll();
    }

    @Test
    public void testWrongEventData() {
        EventHandler handler = EventHandler.create().eventData("TEST_KEY", "TEST_VALUE").build();
        expect(event.getEventField("TEST_KEY")).andReturn("WRONG_VALUE").anyTimes();

        replayAll();
        executeHandlerTest(handler, h -> assertEquals(false, h.getHandlerCondition().test(event)));
        verifyAll();
    }

    @Test
    public void testEventDataLike() {
        EventHandler handler = EventHandler.create().eventDataLike("TEST_KEY", ".*KEY.*").build();
        expect(event.getEventField("TEST_KEY")).andReturn("TEST_KEY").anyTimes();
        Event event2 = createMock(Event.class);
        expect(event2.getEventField("TEST_KEY")).andReturn("TEST").anyTimes();

        replayAll();
        executeHandlerTest(handler, h -> {
            assertEquals(true, handler.getHandlerCondition().test(event));
            assertEquals(false, handler.getHandlerCondition().test(event2));
        });

        verifyAll();
    }

    @Test
    public void testHasDataKey() {
        EventHandler handler = EventHandler.create().hasDataKey("TEST_KEY").build();
        expect(event.getEventField("TEST_KEY")).andReturn("SOMETHING_NOT_NULL").anyTimes();
        Event event2 = createNiceMock(Event.class);

        replayAll();
        executeHandlerTest(handler, h -> {
            assertEquals(true, h.getHandlerCondition().test(event));
            assertEquals(false, h.getHandlerCondition().test(event2));
        });
        verifyAll();
    }

    @Test
    public void testEventHandlerEventType() {
        EventHandler handler = EventHandler.create().eventType("TEST_TYPE").build();
        expect(event.getEventType()).andReturn("TEST_TYPE").anyTimes();

        replayAll();
        executeHandlerTest(handler, h -> assertEquals(true, handler.getHandlerCondition().test(event)));
        verifyAll();
    }

    @Test
    public void testEventHandlerWrongType() {
        EventHandler handler = EventHandler.create().eventType("TEST_TYPE1").build();
        expect(event.getEventType()).andReturn("TEST_TYPE").anyTimes();

        replayAll();
        executeHandlerTest(handler, h -> assertEquals(false, handler.getHandlerCondition().test(event)));
        verifyAll();
    }

    @Test
    public void testEventHandlerEventName() {
        EventHandler handler = EventHandler.create().eventType("NAME_TEST").build();
        expect(event.getEventType()).andReturn("NAME_TEST").anyTimes();

        replayAll();
        executeHandlerTest(handler, h -> assertEquals(true, h.getHandlerCondition().test(event)));

        verifyAll();
    }

    @Test
    public void testJsonEventHandlerTransmission() {
        EventHandler original = EventHandler.create().eventName("NAME").eventType("TYPE").eventData("DATA_KEY", "DATA_VALUE").build();
        EventHandler copied = EventHandler.fromJson(original.encodeToJson()).orElse(null);
        expect(event.getEventName()).andReturn("NAME").anyTimes();
        expect(event.getEventType()).andReturn("TYPE").anyTimes();
        expect(event.getEventField("DATA_KEY")).andReturn("DATA_VALUE").anyTimes();

        replayAll();
        assertEquals(true, original.getHandlerCondition().test(event));
        assertEquals(true, copied.getHandlerCondition().test(event));
        assertEquals(original.getEventHandlerId(), copied.getEventHandlerId());
        verifyAll();
    }

    @Test
    public void testJsonEventHandlerTransmissionNonMatchingEvent() {
        EventHandler original = EventHandler.create().eventName("NAME").eventType("TYPE").eventData("DATA_KEY", "DATA_VALUE").build();
        String eventHandlerJson = original.encodeToJson();
        EventHandler copied = EventHandler.fromJson(eventHandlerJson).orElse(null);
        expect(event.getEventName()).andReturn("NAME1").anyTimes();
        expect(event.getEventType()).andReturn("TYPE").anyTimes();
        expect(event.getEventField("DATA_KEY")).andReturn("DATA_VALUE").anyTimes();

        replayAll();
        assertEquals(false, original.getHandlerCondition().test(event));
        assertEquals(false, copied.getHandlerCondition().test(event));
        assertEquals(original.getEventHandlerId(), copied.getEventHandlerId());
        verifyAll();
    }

    @Test
    public void testEventHandlerJsonTransmissionDataEqual() {
        EventHandler original = EventHandler.create().eventName("NAME").eventType("TYPE").eventData("DATA_KEY", "DATA_VALUE").build();
        String eventHandlerJson = original.encodeToJson();
        EventHandler copied = EventHandler.fromJson(eventHandlerJson).orElse(null);
        String copiedJson = copied.encodeToJson();
        assertEquals(eventHandlerJson, copiedJson);
    }
}
