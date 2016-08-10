package integration;

import api.events.event_queue.event_queue_interface.EventQueueInterface;
import impl.events.event_queue.event_queue_interface.BufferedEventQueueInterface;

/**
 * Created by chris on 7/14/16.
 */
public class BufferedEventQueueInterfaceInteractionTest {
    private EventQueueInterface bufferedEventQueueInterface = new BufferedEventQueueInterface();
    private EventQueueInterface bufferedEventQueueInterface2 = new BufferedEventQueueInterface();
}
