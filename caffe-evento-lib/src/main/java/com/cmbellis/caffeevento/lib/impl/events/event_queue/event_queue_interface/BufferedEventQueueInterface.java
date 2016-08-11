package com.cmbellis.caffeevento.lib.impl.events.event_queue.event_queue_interface;

import com.cmbellis.caffeevento.lib.annotation.CEExport;
import com.cmbellis.caffeevento.lib.api.events.*;
import com.cmbellis.caffeevento.lib.api.events.event_queue.EventQueue;
import com.cmbellis.caffeevento.lib.api.lib.SetLogger;
import com.cmbellis.caffeevento.lib.impl.events.EventSourceImpl;
import com.cmbellis.caffeevento.lib.impl.events.event_queue.SynchronousEventQueue;
import com.cmbellis.caffeevento.lib.impl.lib.AutoRotatedSetLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by chris on 7/13/16.
 */
@CEExport
public class BufferedEventQueueInterface extends EventQueueInterfaceImpl implements EventSink{
    protected EventSource internalEventGenerator = new EventSourceImpl();
    private EventSource externalEventGenerator = new EventSourceImpl();
    protected List<EventHandler> eventHandlers = new ArrayList<>();
    protected List<EventSource> eventSources = new ArrayList<>();
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
        eventHandlers.add(theEventHandler);
        bufferEventQueue.addEventHandler(theEventHandler);
    }

    @Override
    public void removeEventHandler(EventHandler theEventHandler) {
        eventHandlers.remove(theEventHandler);
        bufferEventQueue.removeEventHandler(theEventHandler);
    }

    @Override
    public void removeEventHandler(UUID handlerId) {
        eventHandlers.stream().filter(h -> h.getEventHandlerId().equals(handlerId)).forEach(this::removeEventHandler);
    }

    @Override
    public void addEventSource(EventSource eventSource) {
        eventSources.add(eventSource);
        eventSource.addListener(this);
        bufferEventQueue.addEventSource(eventSource);
    }

    @Override
    public void removeEventSource(EventSource eventSource) {
        eventSources.add(eventSource);
        eventSource.addListener(this);
        bufferEventQueue.addEventSource(eventSource);
    }

    @Override
    public void removeEventSource(UUID sourceId) {
        eventSources.stream().filter(s -> s.getEventSourceId().equals(sourceId)).forEach(this::removeEventSource);
    }

    @Override
    public void receiveEvent(Event e) {
        eventLogger.add(e.getEventId());
        internalEventGenerator.registerEvent(e);
        externalEventGenerator.registerEvent(e);
    }
}
