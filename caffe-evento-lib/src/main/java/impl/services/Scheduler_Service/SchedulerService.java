package impl.services.Scheduler_Service;

import api.events.EventHandler;
import api.events.EventSource;
import api.events.event_queue.EventQueue;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.services.Service;
import api.utils.EventBuilder;
import impl.events.EventSourceImpl;
import impl.events.event_queue.FirstHandlerOnly;
import impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import impl.services.AbstractService;
import impl.services.Scheduler_Service.Scheduling_Services.SchedulingService;
import impl.services.sched_service.SchedService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by eric on 7/22/16.
 */
public class SchedulerService extends AbstractService {
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
        private final EventQueue internalQueue;

    /* code */
    SchedulerService(EventQueueInterface eventQueueInterface){
        super(eventQueueInterface);
        getEventQueueInterface().addEventSource(eventGenerator);
        internalQueue = new FirstHandlerOnly(event->{
            log.error("No compatible Scheduler for: " + event.encodeEvent());
            eventGenerator.registerEvent(
                    EventBuilder.create()
                            .type(SCHEDULER_ERROR)
                            .name("No compatible Scheduler for: " + event.getEventName())
                            .data(SCHEDULER_ID_FIELD, event.getEventField(SCHEDULER_ID_FIELD))
                            .data("Details", event.encodeEvent())
                            .build()
            );
        });
        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventType(SCHEDULE_EVENT)
                .hasDataKey(FORMAT)
                .hasDataKey(ARGS)
                .hasDataKey(SCHEDULER_ID_FIELD)
                .hasDataKey(SCHEDULED_ACTION)
                .eventHandler(internalQueue::receiveEvent)
                .build()
        );
    }

    public void addScheduler(SchedulingService schedulingService) {
        internalQueue.addEventQueueInterface(schedulingService.getSchedulingEventQueueInterface());
    }

    public void removeScheduler(SchedulingService schedulingService) {
        schedulingService.getSchedulingHandlers()
                .forEach(internalQueue::removeEventHandler);
    }

    private class schedulerJob implements Job {
        public schedulerJob(){}
        public void execute(JobExecutionContext context)
                throws JobExecutionException {
            //This part does the thing
        }
    }
}
