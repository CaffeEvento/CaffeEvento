package impl.services.scheduler_service;

import api.events.Event;
import api.events.EventHandler;
import api.events.EventSource;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.utils.EventBuilder;
import impl.events.EventHandlerImpl.EventHandlerBuilder;
import impl.events.EventSourceImpl;
import impl.events.event_queue.FirstHandlerOnly;
import impl.services.container_service.ServiceContainerEventQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by eric on 7/22/16.
 */
public class SchedulerContainerService extends ServiceContainerEventQueue {
    /* constants */
        /* mandatory fields */
        public static final String FORMAT = "SCHEDULER_FORMAT";
        public static final String ARGS = "SCHEDULER_ARGS";
        public static final String SCHEDULER_ID_FIELD = "SCHEDULER_ID";
        public static final String SCHEDULED_ACTION = "SCHEDULED_EVENT";
        /* pick one eventType */
        public static final String SCHEDULE_EVENT = "SCHEDULE";
        public static final String UNSCHEDULE_EVENT = "UNSCHEDULE";
        /* pick one reply */
        public static final String SCHEDULER_ERROR = "UNSCHEDULABLE_EVENT";
        public static final String UNSCHEDULED_ACTION = "UNSCHEDULED";
        public static final String BAD_SCHEDULER = "SCHEDULER_FAILURE";

    /* finals */
        private static final Log log = LogFactory.getLog(SchedulerContainerService.class);
        private final EventSource eventGenerator = new EventSourceImpl();

    /* code */
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

    public static EventBuilder couldNotSchedule(Event event, String reason) {
        return EventBuilder.create()
                .type(SCHEDULER_ERROR)
                .name(reason + event.getEventName())
                .data(SCHEDULER_ID_FIELD, event.getEventField(SCHEDULER_ID_FIELD))
                .data("Details", event.encodeEvent());
    }
}
