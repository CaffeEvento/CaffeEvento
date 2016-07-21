package api.events.event_queue.event_queue_interface;

import api.events.EventHandler;
import api.events.EventSource;

/**
 * Created by chris on 7/1/16.
 */
public interface EventQueueInterfaceChangedListener {
    void removeEventHandler(EventHandler theEventHandler);
    void addEventHandler(EventHandler theEventHandler);

    void removeEventSource(EventSource theEventSource);
    void addEventSource(EventSource theEventSource);
}
