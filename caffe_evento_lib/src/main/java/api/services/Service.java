package api.services;

import api.events.event_queue.event_queue_interface.EventQueueInterface;

/**
 * Created by chris on 7/13/16.
 */
public interface Service {
    EventQueueInterface getEventQueueInterface();
}
