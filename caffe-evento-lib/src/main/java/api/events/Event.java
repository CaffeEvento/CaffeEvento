package api.events;

import com.google.gson.GsonBuilder;
import impl.events.EventImpl;

import java.io.Reader;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by chris on 7/13/16.
 */
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

    public static Event decodeEvent(String theEvent) {
        return (new GsonBuilder()).create().fromJson(theEvent, EventImpl.class);
    }

    public static Event decodeEvent(Reader theEvent) {
        return (new GsonBuilder()).create().fromJson(theEvent, EventImpl.class);
    }
}
