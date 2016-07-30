package impl.services.request_service;

import impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;

/**
 * Created by chris on 7/28/16.
 */
public class RequestServiceFactory {
    public static RequestService getService() {
        return new RequestService(new EventQueueInterfaceImpl());
    }
}
