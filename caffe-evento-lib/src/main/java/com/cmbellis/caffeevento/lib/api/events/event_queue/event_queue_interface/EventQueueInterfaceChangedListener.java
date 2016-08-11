package com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface;

import com.cmbellis.caffeevento.lib.api.events.EventHandler;
import com.cmbellis.caffeevento.lib.api.events.EventSource;

/**
 * Created by chris on 7/1/16.
 */
public interface EventQueueInterfaceChangedListener {
    void removeEventHandler(EventHandler theEventHandler);
    void addEventHandler(EventHandler theEventHandler);

    void removeEventSource(EventSource theEventSource);
    void addEventSource(EventSource theEventSource);
}
