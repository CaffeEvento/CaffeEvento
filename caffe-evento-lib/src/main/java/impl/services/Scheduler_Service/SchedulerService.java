package impl.services.Scheduler_Service;

import api.events.Event;
import api.events.EventHandler;
import api.events.EventSource;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.utils.EventBuilder;
import impl.events.EventSourceImpl;
import impl.events.event_queue.FirstHandlerOnly;
import impl.services.Container_Services.ServiceContainerEventQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by eric on 7/22/16.
 */
public class SchedulerService extends ServiceContainerEventQueue {
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

    /* finals */
        private static final Log log = LogFactory.getLog(SchedulerService.class);
        private final EventSource eventGenerator = new EventSourceImpl();

    /* code */
    SchedulerService(EventQueueInterface eventQueueInterface){
        super(eventQueueInterface, FirstHandlerOnly::new);
    }

    @Override
    protected void elevate(Event event) {
        if(searchCriteria().getHandlerCondition().test(event)) {
            log.error("No compatible Scheduler for: " + event.encodeEvent());
            EventBuilder.create()
                    .type(SCHEDULER_ERROR)
                    .name("No compatible Scheduler for: " + event.getEventName())
                    .data(SCHEDULER_ID_FIELD, event.getEventField(SCHEDULER_ID_FIELD))
                    .data("Details", event.encodeEvent())
                    .send(elevateGenerator);
        } else {
            elevateGenerator.registerEvent(event);
        }
    }

    @Override
    protected EventHandler searchCriteria(){
        return EventHandler.create()
                .eventType(SCHEDULE_EVENT)
                .hasDataKey(FORMAT)
                .hasDataKey(ARGS)
                .hasDataKey(SCHEDULER_ID_FIELD)
                .hasDataKey(SCHEDULED_ACTION)
                .eventHandler(this::receiveEvent)
                .build();
    }

    public static Event couldNotSchedule(Event event, String reason) {
        return EventBuilder.create()
                .type(SCHEDULER_ERROR)
                .name(reason + event.getEventName())
                .build();
    }
}
