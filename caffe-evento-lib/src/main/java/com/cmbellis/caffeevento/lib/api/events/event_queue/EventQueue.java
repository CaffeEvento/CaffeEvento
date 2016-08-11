package com.cmbellis.caffeevento.lib.api.events.event_queue;

import com.cmbellis.caffeevento.lib.annotation.CEExport;
import com.cmbellis.caffeevento.lib.api.events.EventSink;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterfaceChangedListener;
import com.cmbellis.caffeevento.lib.api.services.Service;

/**
 * Created by chris on 7/10/16.
 */
@CEExport
public interface EventQueue extends EventQueueInterfaceChangedListener, EventSink {

    void registerService(Service theService);

    void unRegisterService(Service theService);

    void addEventQueueInterface(EventQueueInterface theEventQueueInterface);

    void removeEventQueueInterface(EventQueueInterface theEventQueueInterface);
}
