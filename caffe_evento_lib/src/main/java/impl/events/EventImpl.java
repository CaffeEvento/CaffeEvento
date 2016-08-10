package impl.events;

import api.events.Event;
import com.google.gson.GsonBuilder;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by chris on 7/1/16.
 */
public class EventImpl implements Event {
    private String eventName;
    private String eventType;
    private UUID eventId = UUID.randomUUID();
    private Map<String, String> eventDetails = new HashMap<>();
    private Date timestamp = Date.from(Instant.now());

    public EventImpl(){
        eventName = null;
        eventType = null;
    }

    public EventImpl(String eventName, String eventType){
        this.eventName = eventName;
        this.eventType = eventType;
    }

    public EventImpl(Event e) {
        this.eventName = e.getEventName();
        this.eventType = e.getEventType();
        this.eventDetails = e.getEventDetails();
    }

    @Override
    public String getEventName(){
        return this.eventName;
    }
    @Override
    public void setEventName(String name){
        this.eventName = name;
    }

    @Override
    public String getEventType() { return this.eventType; }
    @Override
    public void setEventType(String type) { this.eventType = type; }

    @Override
    public void setEventField(String field, String value) {
        eventDetails.put(field, value);
    }
    @Override
    public String getEventField(String field) {
        return eventDetails.get(field);
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Map<String, String> getEventDetails() {
        return new HashMap<>(eventDetails);
    }

    @Override
    public Date getEventTimestamp() {
        return timestamp;
    }

    @Override
    public String encodeEvent() {
        return (new GsonBuilder()).create().toJson(this);
    }

}
