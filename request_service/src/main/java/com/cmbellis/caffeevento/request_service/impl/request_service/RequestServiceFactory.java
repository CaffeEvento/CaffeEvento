package com.cmbellis.caffeevento.request_service.impl.request_service;

import com.cmbellis.caffeevento.lib.impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;

/**
 * Created by chris on 7/28/16.
 */
public class RequestServiceFactory {
    public static RequestService getService() {
        return new RequestService(new EventQueueInterfaceImpl());
    }
}
