package com.cmbellis.caffeevento.lib.api.utils;

import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.events.EventSource;
import com.cmbellis.caffeevento.lib.impl.events.EventImpl;

import java.util.function.Consumer;

/**
 * Created by chris on 7/12/16.
 */
public class EventBuilder {
    private EventBuilder() {
        this.event = new EventImpl();
    }
    private Event event;

    public static EventBuilder create() {
        return new EventBuilder();
    }

    public EventBuilder name(String name) {
        this.event.setEventName(name);
        return this;
    }

    public EventBuilder type(String type) {
        this.event.setEventType(type);
        return this;
    }

    public EventBuilder data(String key, String value) {
        this.event.setEventField(key, value);
        return this;
    }

    public Event build() {
        return this.event;
    }

    public void build(Consumer<Event> consumer) {
        consumer.accept(build());
    }

    public void send(EventSource source) {
        source.registerEvent(build());
    }
}
