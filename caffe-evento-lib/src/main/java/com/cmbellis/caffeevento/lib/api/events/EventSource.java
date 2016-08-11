package com.cmbellis.caffeevento.lib.api.events;

import com.cmbellis.caffeevento.lib.annotation.CEExport;

import java.util.UUID;

/**
 * Created by chris on 7/13/16.
 */
@CEExport
public interface EventSource {
    void addListener(EventSink theEventSink);

    void removeListener(EventSink theEventSink);

    void registerEvent(Event theEvent);

    UUID getEventSourceId();
}
