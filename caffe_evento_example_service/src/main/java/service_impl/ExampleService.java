package service_impl;

import api.events.event_queue.event_queue_interface.EventQueueInterface;
import impl.services.AbstractService;

/**
 * A service that literally does nothing
 * Created by chris on 8/10/16.
 */
public class ExampleService extends AbstractService {
    public ExampleService(EventQueueInterface eventQueueInterface) {
        super(eventQueueInterface);
    }
}
