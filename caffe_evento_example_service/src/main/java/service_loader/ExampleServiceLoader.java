package service_loader;

import api.service_loader.CEFramework;
import api.service_loader.CEServiceLoader;
import api.services.Service;
import impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import service_impl.ExampleService;

/**
 * This is responsible for creating the service
 * Created by chris on 8/10/16.
 */
public class ExampleServiceLoader implements CEServiceLoader {
    @Override
    public Service loadService(CEFramework framework) {
        return new ExampleService(new EventQueueInterfaceImpl());
    }
}
