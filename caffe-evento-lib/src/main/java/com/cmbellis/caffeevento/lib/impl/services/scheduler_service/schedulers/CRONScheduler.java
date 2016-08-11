package com.cmbellis.caffeevento.lib.impl.services.scheduler_service.schedulers;

import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import org.quartz.*;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.text.ParseException;

import static org.quartz.JobBuilder.newJob;

/**
 * Created by eric on 7/28/16.
 */
public class CRONScheduler extends AbstractQuartzScheduler {
    public static final String format = "CRON";

    public CRONScheduler(EventQueueInterface eventQueueInterface, Scheduler scheduler){
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