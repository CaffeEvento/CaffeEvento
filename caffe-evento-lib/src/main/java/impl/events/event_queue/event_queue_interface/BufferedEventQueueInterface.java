package impl.events.event_queue.event_queue_interface;

import api.events.*;
import api.events.event_queue.EventQueue;
import api.lib.SetLogger;
import impl.events.EventSourceImpl;
import impl.events.event_queue.SynchronousEventQueue;
import impl.lib.AutoRotatedSetLogger;

import java.util.UUID;

/**
 * Created by chris on 7/13/16.
 */
public class BufferedEventQueueInterface extends EventQueueInterfaceImpl implements EventSink{
    protected EventSource internalEventGenerator = new EventSourceImpl();
    private EventSource externalEventGenerator = new EventSourceImpl();
    private SetLogger<UUID> eventLogger;
    private EventQueue bufferEventQueue;

    public BufferedEventQueueInterface() {
        this(new SynchronousEventQueue(), new AutoRotatedSetLogger<>());

    }

    BufferedEventQueueInterface(EventQueue internalEventQueue, SetLogger<UUID> eventLogger) {
        this.bufferEventQueue = internalEventQueue;
        this.eventLogger = eventLogger;

        // Forward all external events to the internal event queue
        internalEventQueue.addEventSource(internalEventGenerator);

        // Add an event handler that only registers the event if we don't already have it
        super.addEventHandler(EventHandler.create().eventHandler(e -> {
            if(!eventLogger.contains(e.getEventId())) {
                internalEventGenerator.registerEvent(e);
            }
        }).build());

        // Register an external event generator to send events to external things (might not be used in standalone)
        super.addEventSource(externalEventGenerator);
    }

    @Override
    public void addEventHandler(EventHandler theEventHandler) {
        bufferEventQueue.addEventHandler(theEventHandler);
    }

    @Override
    public void removeEventHandler(EventHandler theEventHandler) {
        bufferEventQueue.removeEventHandler(theEventHandler);
    }

    @Override
    public void addEventSource(EventSource eventSource) {
        eventSource.addListener(this);
        bufferEventQueue.addEventSource(eventSource);
    }

    @Override
    public void removeEventSource(EventSource eventSource) {
        eventSource.addListener(this);
        bufferEventQueue.addEventSource(eventSource);
    }

    @Override
    public void receiveEvent(Event e) {
        eventLogger.add(e.getEventId());
        internalEventGenerator.registerEvent(e);
        externalEventGenerator.registerEvent(e);
    }
}
