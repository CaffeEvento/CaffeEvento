package com.cmbellis.caffeevento.lib.api.events;

import com.cmbellis.caffeevento.lib.annotation.CEExport;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.cmbellis.caffeevento.lib.impl.events.EventImpl;

import java.io.Reader;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by chris on 7/13/16.
 */
@CEExport
public interface Event {

    String getEventName();

    void setEventName(String name);

    String getEventType();

    void setEventType(String type);

    void setEventField(String field, String value);

    String getEventField(String field);

    UUID getEventId();

    Map<String, String> getEventDetails();

    Date getEventTimestamp();

    String encodeEvent();

    public static Optional<Event> decodeEvent(String theEvent) {
        try {
            return Optional.of((new GsonBuilder()).create().fromJson(theEvent, EventImpl.class));
        } catch (JsonSyntaxException e) {
            return Optional.empty();
        }
    }

    public static Optional<Event> decodeEvent(Reader theEvent) {
        try {
            return Optional.of((new GsonBuilder()).create().fromJson(theEvent, EventImpl.class));
        } catch (JsonSyntaxException e) {
            return Optional.empty();
        }
    }
}
