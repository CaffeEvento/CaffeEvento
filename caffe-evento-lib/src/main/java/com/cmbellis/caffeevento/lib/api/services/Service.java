package com.cmbellis.caffeevento.lib.api.services;

import com.cmbellis.caffeevento.lib.annotation.CEExport;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;

/**
 * Created by chris on 7/13/16.
 */
@CEExport
public interface Service {
    EventQueueInterface getEventQueueInterface();
}
