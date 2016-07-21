package api.events.event_queue.event_queue_interface;

import api.events.EventHandler;
import api.events.EventSource;

import java.util.List;
import java.util.UUID;

/**
 * Created by chris on 7/13/16.
 */
public interface EventQueueInterface {
    /**
     * Get event sources from the event queue interface
     * @return List of event sources
     */
    List<EventSource> getEventSources();

    /**
     * Get event handlers from the event queue interface
     * @return List of event handlers
     */
    List<EventHandler> getEventHandlers();

    /**
     * Adds a subscriber to this event queue interface
     * @param theEventQueueInterfaceChangedListener
     */
    void addEventQueueInterfaceChangedListener(EventQueueInterfaceChangedListener theEventQueueInterfaceChangedListener);

    /**
     * Removes a subscriber to this event queue interface
     * @param theEventQueueInterfaceChangedListener
     */
    void removeEventQueueInterfaceChangedListener(EventQueueInterfaceChangedListener theEventQueueInterfaceChangedListener);

    /**
     * Removes an event source from the event queue interface, using its ID
     * @param id uniquely identifies the event source to remove
     */
    void removeEventSource(UUID id);

    void addEventSource(EventSource theEventSource);

    void removeEventSource(EventSource theEventSource);

    void removeEventHandler(UUID id);

    void addEventHandler(EventHandler theEventHandler);

    void removeEventHandler(EventHandler theEventHandler);
}
