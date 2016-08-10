package impl.services.scheduler_service.schedulers;

import api.events.Event;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import impl.events.EventImpl;
import org.quartz.*;

import java.util.Optional;

import static org.quartz.JobBuilder.newJob;

/**
 * Created by eric on 8/9/16.
 */
public abstract class AbstractQuartzScheduler extends AbstractScheduler {
    private final Scheduler scheduler;

    protected abstract Trigger createTrigger(String args);

    protected Schedule scheduleJob(String args, String action, String id) {
        return new quartzSchedule(args, action, id);
    }

    public AbstractQuartzScheduler(EventQueueInterface eventQueueInterface, String format, Scheduler scheduler){
        super(eventQueueInterface, format);
        this.scheduler = scheduler;
    }

    // Job to schedule with quartz
    public class JobRegistersAction implements Job {
        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            System.err.println("Oops");
            Optional<Event> action = Event.decodeEvent(jobExecutionContext
                    .getJobDetail()
                    .getJobDataMap()
                    .getString("action"))
                    .map(EventImpl::new);
//            log.debug(action.map(Event::encodeEvent).orElse("T"));
            action.ifPresent(eventGenerator::registerEvent); //TODO: eventGenerator is provided by AbstractScheduler, this is causing headaches during testing
        }
    }

    // implementation of Schedule for Quartz
    private class quartzSchedule extends Schedule {
        private final JobDetail job;
        private final Trigger theTrigger;

        @Override
        protected void cancelJob() {
            try {
                scheduler.deleteJob(job.getKey());
            }catch(org.quartz.SchedulerException e){
                log.error(e);
            }
        }

        @Override
        protected void startJob(){
            try {
                scheduler.scheduleJob(job, theTrigger);
            }catch(org.quartz.SchedulerException e) {
                log.error(e);
                throw new CESchedulerException("Problem Scheduling Action");
            }
        }

        public quartzSchedule(String args, String action, String Id) {
            super(Id);
            job = newJob()
                    .ofType(JobRegistersAction.class)
                    .withIdentity(id)
                    .usingJobData("action", action)
                    .build();
            theTrigger = createTrigger(args);
        }
    }
}
