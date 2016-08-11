package com.cmbellis.caffeevento.lib.api.utils;

import com.cmbellis.caffeevento.lib.api.events.Event;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by chris on 7/16/16.
 */
public class EventBuilderTest {
    @Test
    public void name() throws Exception {
        Event e = EventBuilder.create().name("TEST_NAME").build();
        assertEquals(e.getEventName(), "TEST_NAME");
    }

    @Test
    public void type() throws Exception {
        Event e = EventBuilder.create().type("TEST_TYPE").build();
        assertEquals(e.getEventType(), "TEST_TYPE");
    }

    @Test
    public void data() throws Exception {
        Event e = EventBuilder.create().data("TEST_KEY", "TEST_VALUE").build();
        assertEquals(e.getEventField("TEST_KEY"), "TEST_VALUE");
    }

}