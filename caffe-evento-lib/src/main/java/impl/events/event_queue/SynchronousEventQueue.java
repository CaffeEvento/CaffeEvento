package impl.events.event_queue;

import api.events.*;
import impl.events.event_queue.AbstractEventQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 7/1/16.
 */
public class SynchronousEventQueue extends AbstractEventQueue {

    public SynchronousEventQueue() {
    }

    @Override
    public synchronized void receiveEvent(Event e) {
        List<EventHandler> tempEventHandlers = new ArrayList<>(eventHandlers);
        tempEventHandlers.stream()
                .filter(handler -> handler.getHandlerCondition().test(e))
                .forEach(handler -> handler.handleEvent(e));
    }
}
