package impl.services.scheduler_service.schedulers;

import api.events.Event;
import api.events.EventHandler;
import api.events.EventSource;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.utils.EventBuilder;
import impl.events.EventImpl;
import impl.events.EventSourceImpl;
import impl.services.AbstractService;
import impl.services.scheduler_service.SchedulerContainerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.quartz.JobBuilder.newJob;

/**
 * Created by eric on 7/28/16.
 */
abstract public class AbstractScheduler extends AbstractService {
    private final String format;

    protected Log log = LogFactory.getLog(getClass());
    private Scheduler scheduler;
    private EventSource eventGenerator = new EventSourceImpl();
    private Map<String, EventHandler> activeJobs = new ConcurrentHashMap<>();

    //only these next functions should be unique between schedulers using quartz
    abstract protected Trigger createTrigger(String args);
    //
    abstract protected boolean validateArgs(String args);

    AbstractScheduler(EventQueueInterface eventQueueInterface, String format, Scheduler externSchueduler) {
        super(eventQueueInterface);
        this.scheduler = externSchueduler;
        this.format = format;
        getEventQueueInterface().addEventSource(eventGenerator);
            // registering the event handler is inside the try block so that it fails if the scheduler cannot start
            getEventQueueInterface().addEventHandler(EventHandler.create()
                    .eventData(SchedulerContainerService.FORMAT, this.format)
                    .hasDataKey(SchedulerContainerService.ARGS)
                    .hasDataKey(SchedulerContainerService.SCHEDULED_ACTION)
                    .hasDataKey(SchedulerContainerService.SCHEDULER_ID_FIELD)
                    .eventHandler(event -> {
                        if(validateArgs(event.getEventField(SchedulerContainerService.ARGS)) &&
                                Event.decodeEvent(event.getEventField(SchedulerContainerService.SCHEDULED_ACTION))
                                        .isPresent()){
                            try {
                                new Schedule(event.getEventField(SchedulerContainerService.ARGS),
                                        event.getEventField(SchedulerContainerService.SCHEDULED_ACTION),
                                        event.getEventField(SchedulerContainerService.SCHEDULER_ID_FIELD));
                            }catch(CESchedulerException e){
                                log.error(e);
                                SchedulerContainerService.couldNotSchedule(event, "Problem with scheduling: ")
                                        .data("ErrorMessage",e.toString())
                                        .send(eventGenerator);
                            }
                        } else {
                            SchedulerContainerService.couldNotSchedule(event,"Could not schedule; " +
                                    "Invalid ARGS or malformed action: ")
                                    .send(eventGenerator);
                        }
                    }).build());
    }

    public EventBuilder createScheduleEvent(String args, Event action, UUID schedulerID) {
        return EventBuilder.create()
                .type(SchedulerContainerService.SCHEDULE_EVENT)
                .data(SchedulerContainerService.FORMAT, this.format)
                .data(SchedulerContainerService.ARGS, args)
                .data(SchedulerContainerService.SCHEDULED_ACTION, action.encodeEvent())
                .data(SchedulerContainerService.SCHEDULER_ID_FIELD, schedulerID.toString());
    }

    public int countActiveJobs() {
        return activeJobs.size();
    }

    protected class CESchedulerException extends RuntimeException {
        CESchedulerException(String message) {
            super(message);
        }
    }

    private class Schedule {
        JobDetail job;
        Trigger trigger;

        class JobRegistersAction implements Job {
            @Override
            public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
                Event.decodeEvent(
                        jobExecutionContext
                                .getJobDetail()
                                .getJobDataMap()
                                .getString("action")
                ).ifPresent(event ->
                        eventGenerator.registerEvent(new EventImpl(event))
                );
            };
        }

        Schedule(String args, String action, String Id){
            job = newJob()
                    .ofType(JobRegistersAction.class)
                    .withIdentity(Id)
                    .usingJobData("action", action)
                    .build();
            trigger = createTrigger(args);

            try {
                scheduler.scheduleJob(job, trigger);
            }catch(org.quartz.SchedulerException e) {
                log.error(e);
                throw new CESchedulerException("Problem Scheduling Action");
            }
            // Register a handler to cancel the event
            EventHandler cancel = EventHandler.create()
                    .eventType(SchedulerContainerService.UNSCHEDULE_EVENT)
                    .eventData(SchedulerContainerService.SCHEDULER_ID_FIELD, Id)
                    .eventHandler(event -> {
                        try {
                            scheduler.deleteJob(job.getKey());
                        }catch(org.quartz.SchedulerException e){
                            log.error(e);
                        }
                        getEventQueueInterface().removeEventHandler(activeJobs
                                .get(Id)
                                .getEventHandlerId());
                        activeJobs.remove(Id);
                        EventBuilder.create()
                                .type(SchedulerContainerService.UNSCHEDULE_EVENT)
                                .name("Unscheduled" + Id)
                                .data(SchedulerContainerService.SCHEDULER_ID_FIELD, Id)
                                .send(eventGenerator);
                    }).build();
            activeJobs.put(Id, cancel);
            getEventQueueInterface().addEventHandler(cancel);
        }
    }
}
