package impl.services.log_service;

import api.events.EventSource;
import api.events.event_queue.EventQueue;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import impl.events.EventSourceImpl;
import impl.events.event_queue.SynchronousEventQueue;
import impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import impl.services.request_service.RequestService;
import org.apache.commons.logging.Log;
import org.powermock.api.easymock.annotation.Mock;
import test_util.EventCollector;

import static org.junit.Assert.*;

/**
 * Created by chris on 7/31/16.
 */
public class LogServiceTest {
    @Mock
    private Log log;
    private EventQueue eventQueue = new SynchronousEventQueue();
    private EventQueueInterface eventQueueInterface = new EventQueueInterfaceImpl();
    private LogService instance = new LogService(eventQueueInterface, log);
    private EventCollector eventCollector = new EventCollector();
    private EventSource eventGenerator = new EventSourceImpl();

}