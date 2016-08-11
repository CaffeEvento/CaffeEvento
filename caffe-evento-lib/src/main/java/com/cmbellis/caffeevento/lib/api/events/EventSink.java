package com.cmbellis.caffeevento.lib.api.events;

import com.cmbellis.caffeevento.lib.annotation.CEExport;

/**
 * Created by chris on 7/13/16.
 */
@CEExport
public interface EventSink {
    void receiveEvent(Event e);
}
