package impl.services.scheduler_service.schedulers;

import api.events.Event;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.utils.EventBuilder;
import impl.services.scheduler_service.SchedulerContainerService;
import org.quartz.*;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.text.ParseException;
import java.util.UUID;

import static org.quartz.JobBuilder.newJob;

/**
 * Created by eric on 7/28/16.
 */
public class CRONScheduler extends AbstractQuartzScheduler {
    public static final String format = "CRON";

    CRONScheduler(EventQueueInterface eventQueueInterface, Scheduler scheduler){
        super(eventQueueInterface, format, scheduler);
    }

    @Override
    protected Trigger createTrigger(String args) {
        CronTriggerImpl cronTrigger = new CronTriggerImpl();
        try {
            cronTrigger.setCronExpression(args);
        }catch(ParseException e){
            log.error(e);
            throw new CESchedulerException("Unable to parse args.");
        }
        return cronTrigger;
    }

    @Override
    protected boolean validateArgs(String args) {
        return CronExpression.isValidExpression(args);
    }
}