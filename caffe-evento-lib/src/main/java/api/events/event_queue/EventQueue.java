package api.events.event_queue;

import api.events.EventSink;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.events.event_queue.event_queue_interface.EventQueueInterfaceChangedListener;
import api.services.Service;

/**
 * Created by chris on 7/10/16.
 */
public interface EventQueue extends EventQueueInterfaceChangedListener, EventSink {

    void registerService(Service theService);

    void unRegisterService(Service theService);

    void addEventQueueInterface(EventQueueInterface theEventQueueInterface);

    void removeEventQueueInterface(EventQueueInterface theEventQueueInterface);
}
