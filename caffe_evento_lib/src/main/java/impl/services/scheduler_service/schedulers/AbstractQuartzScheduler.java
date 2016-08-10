package impl.services.scheduler_service.schedulers;

import api.events.Event;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import impl.events.EventImpl;
import org.quartz.*;

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

    AbstractQuartzScheduler(EventQueueInterface eventQueueInterface, String format, Scheduler scheduler){
        super(eventQueueInterface, format);
        this.scheduler = scheduler;
    }

    // implementation of Schedule for Quartz
    private class quartzSchedule extends Schedule {
        private final JobDetail job;
        private final Trigger theTrigger;

        class JobRegistersAction implements Job {
            @Override
            public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
                Event.decodeEvent(jobExecutionContext
                        .getJobDetail()
                        .getJobDataMap()
                        .getString("action"))
                        .map(EventImpl::new)
                        .ifPresent(eventGenerator::registerEvent);
            }
        }

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

        quartzSchedule(String args, String action, String Id) {
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
