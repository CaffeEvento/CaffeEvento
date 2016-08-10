package api.events;

/**
 * Created by chris on 7/13/16.
 */
public interface EventSink {
    void receiveEvent(Event e);
}
