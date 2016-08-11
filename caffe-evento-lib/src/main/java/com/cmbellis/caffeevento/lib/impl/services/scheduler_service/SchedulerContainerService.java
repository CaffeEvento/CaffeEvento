package com.cmbellis.caffeevento.lib.impl.services.scheduler_service;

import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.events.EventHandler;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.impl.events.EventHandlerImpl.EventHandlerBuilder;
import com.cmbellis.caffeevento.lib.impl.events.event_queue.FirstHandlerOnly;
import com.cmbellis.caffeevento.lib.impl.services.container_service.ServiceContainerEventQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.cmbellis.caffeevento.lib.impl.services.scheduler_service.Schedules.*;

/**
 * Created by eric on 7/22/16.
 */
public class SchedulerContainerService extends ServiceContainerEventQueue {
    SchedulerContainerService(EventQueueInterface eventQueueInterface){
        super(eventQueueInterface, FirstHandlerOnly::new);
    }

    @Override
    protected void elevate(Event event) {
        if(pullHandlers.stream()
                .map(EventHandler::getHandlerCondition)
                .reduce(Predicate::or).orElse(e->false)
                .test(event)) {
            log.error("No compatible Scheduler for: " + event.encodeEvent());
            couldNotSchedule(event, "No compatible Scheduler for: ")
                    .send(elevateGenerator);
        } else {
            elevateGenerator.registerEvent(event);
        }
    }

    @Override
    protected List<EventHandlerBuilder> pullCriteria() {
        List<EventHandlerBuilder> pullHandlers = new ArrayList<>();
        pullHandlers.add(EventHandler.create()
                .eventType(SCHEDULE_EVENT)
                .hasDataKey(FORMAT)
                .hasDataKey(ARGS)
                .hasDataKey(SCHEDULER_ID_FIELD)
                .hasDataKey(SCHEDULED_ACTION));
        pullHandlers.add(EventHandler.create()
                .eventType(UNSCHEDULE_EVENT)
                .hasDataKey(SCHEDULER_ID_FIELD));
        return pullHandlers;
    }

}
