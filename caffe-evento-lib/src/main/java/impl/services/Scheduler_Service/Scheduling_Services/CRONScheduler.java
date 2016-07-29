package impl.services.Scheduler_Service.Scheduling_Services;

import api.events.Event;
import api.events.EventHandler;
import api.events.EventSource;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.utils.EventBuilder;
import impl.events.EventImpl;
import impl.events.EventSourceImpl;
import impl.services.AbstractService;
import impl.services.Scheduler_Service.SchedulerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.quartz.JobBuilder.newJob;

/**
 * Created by eric on 7/28/16.
 */
public class CRONScheduler extends AbstractService{
    public static final String FORMAT = "CRON";

    private Log log = LogFactory.getLog(getClass());
    private Scheduler scheduler;
    private EventSource eventGenerator = new EventSourceImpl();
    private Map<String, EventHandler> activeJobs = new ConcurrentHashMap<>();

    CRONScheduler(EventQueueInterface eventQueueInterface) {
        super(eventQueueInterface);
        getEventQueueInterface().addEventSource(eventGenerator);
        try{
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            // registering the event handler is inside the try block so that it fails if the scheduler cannot start
            getEventQueueInterface().addEventHandler(EventHandler.create()
                    .eventData(SchedulerService.FORMAT, FORMAT)
                    .hasDataKey(SchedulerService.ARGS)
                    .hasDataKey(SchedulerService.SCHEDULED_ACTION)
                    .hasDataKey(SchedulerService.SCHEDULER_ID_FIELD)
                    .eventHandler(event -> {
                        if(CronExpression.isValidExpression(event.getEventField(SchedulerService.ARGS)) &&
                                Event.decodeEvent(event.getEventField(SchedulerService.SCHEDULED_ACTION))
                                        .isPresent()){
                            try {
                                new cronSchedule(event.getEventField(SchedulerService.ARGS),
                                        event.getEventField(SchedulerService.SCHEDULED_ACTION),
                                        event.getEventField(SchedulerService.SCHEDULER_ID_FIELD));
                            }catch(cronScheduleException e){
                                log.error(e);
                                SchedulerService.couldNotSchedule(event, "Problem with scheduling: ")
                                        .data("ErrorMessage",e.toString())
                                        .send(eventGenerator);
                            }
                        } else {
                            SchedulerService.couldNotSchedule(event,"Could not schedule; " +
                                    "Invalid Cron expression or malformed action: ")
                                    .send(eventGenerator);
                        }
                    }).build());
        }catch(SchedulerException e){
            log.error("Could not create and start a Scheduler: ", e);
            EventBuilder.create()
                    .name(getClass().toString())
                    .type(SchedulerService.BAD_SCHEDULER)
                    .data(SchedulerService.FORMAT, FORMAT)
                    .data("Details", e.toString())
                    .send(eventGenerator);
        }

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        scheduler.shutdown(true);
    }

    public int countActiveJobs() {
        return activeJobs.size();
    }

    private class cronScheduleException extends RuntimeException {
        cronScheduleException(String message) {
            super(message);
        }
    }

    private class cronSchedule {
        JobDetail job;
        Trigger trigger;

        //Of the hidden class; only this next function should be unique between schedulers using quartz
        private Trigger createTrigger(String CRONexpression){
            CronTriggerImpl trig = new CronTriggerImpl();
            try {
                trig.setCronExpression(CRONexpression);
            }catch(ParseException e){
                log.error(e);
                throw new cronScheduleException("Could not parse args: "+CRONexpression);
            }
            return trig;
        }

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

        cronSchedule(String args, String action, String Id){
            job = newJob()
                    .ofType(JobRegistersAction.class)
                    .withIdentity(Id)
                    .usingJobData("action", action)
                    .build();
            trigger = createTrigger(args);

            try {
                scheduler.scheduleJob(job, trigger);
            }catch(SchedulerException e) {
                log.error(e);
                throw new cronScheduleException("Problem Scheduling Action");
            }
            // Register a handler to cancel the event
            EventHandler cancel = EventHandler.create()
                    .eventType(SchedulerService.UNSCHEDULE_EVENT)
                    .eventData(SchedulerService.SCHEDULER_ID_FIELD, Id)
                    .eventHandler(event -> {
                        try {
                            scheduler.deleteJob(job.getKey());
                        }catch(SchedulerException e){
                            log.error(e);
                        }
                        getEventQueueInterface().removeEventHandler(activeJobs
                                .get(Id)
                                .getEventHandlerId());
                        activeJobs.remove(Id);
                        EventBuilder.create()
                                .type(SchedulerService.UNSCHEDULE_EVENT)
                                .name("Unscheduled" + Id)
                                .data(SchedulerService.SCHEDULER_ID_FIELD, Id)
                                .send(eventGenerator);
                    }).build();
            activeJobs.put(Id, cancel);
            getEventQueueInterface().addEventHandler(cancel);
        }
    }
}
