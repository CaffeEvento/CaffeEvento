package com.cmbellis.caffeevento.lib.impl.events.event_queue;

import com.cmbellis.caffeevento.lib.annotation.CEExport;
import com.cmbellis.caffeevento.lib.api.events.Event;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by chris on 7/14/16.
 */
@CEExport
public class FilteredEventQueue extends SynchronousEventQueue {
    private Predicate<Event> eventAcceptCriteria;

    public FilteredEventQueue() {
        this(e -> true);
    }

    public FilteredEventQueue(Predicate<Event> acceptCriteria) {
        this(acceptCriteria, c->{});
    }

    public FilteredEventQueue(Predicate<Event> acceptCriteria, Consumer<Event> eventConsumer) {
        super(eventConsumer);
        this.eventAcceptCriteria = acceptCriteria;
    }

    @Override
    public synchronized void receiveEvent(Event e) {
        if(eventAcceptCriteria.test(e)) super.receiveEvent(e);
    }


}
