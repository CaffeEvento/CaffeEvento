package api.events;

import com.google.gson.GsonBuilder;
import impl.events.EventImpl;

import java.io.Reader;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
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

    public static Optional<Event> decodeEvent(String theEvent) {
        try {
            return Optional.of((new GsonBuilder()).create().fromJson(theEvent, EventImpl.class));
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }

    public static Optional<Event> decodeEvent(Reader theEvent) {
        try {
            return Optional.of((new GsonBuilder()).create().fromJson(theEvent, EventImpl.class));
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }
}
