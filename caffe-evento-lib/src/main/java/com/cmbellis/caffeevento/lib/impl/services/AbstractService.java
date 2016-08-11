package com.cmbellis.caffeevento.lib.impl.services;

import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.api.services.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by chris on 7/13/16.
 */
public abstract class AbstractService implements Service {
    private EventQueueInterface eventQueueInterface;
    protected Log log;

    public AbstractService(EventQueueInterface eventQueueInterface) {
        this.eventQueueInterface = eventQueueInterface;
        log = LogFactory.getLog(getClass());
    }

    @Override
    public EventQueueInterface getEventQueueInterface() {
        return eventQueueInterface;
    }
}
