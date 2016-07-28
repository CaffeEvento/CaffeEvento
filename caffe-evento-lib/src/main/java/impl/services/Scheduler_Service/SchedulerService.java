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
        private static final String FORMAT = "SCHEDULER_FORMAT";
        private static final String ARGS = "SCHEDULER_ARGS";
        private static final String SCHEDULER_ID_FIELD = "SCHEDULER_ID";
        private static final String SCHEDULED_ACTION = "SCHEDULED_EVENT";
        /* pick one eventType */
        private static final String SCHEDULE_EVENT = "SCHEDULE";
        private static final String UNSCHEDULE_EVENT = "UNSCHEDULE";
        /* pick one reply */
        private static final String SCHEDULER_ERROR = "UNSCHEDULABLE_EVENT";
        private static final String UNSCHEDULED_ACTION = "UNSCHEDULED";

    /* finals */
        private static final Log log = LogFactory.getLog(SchedulerService.class);
        private final EventSource eventGenerator = new EventSourceImpl();

    /* code */
    SchedulerService(EventQueueInterface eventQueueInterface){
        super(eventQueueInterface, FirstHandlerOnly::new);
    }

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
}
