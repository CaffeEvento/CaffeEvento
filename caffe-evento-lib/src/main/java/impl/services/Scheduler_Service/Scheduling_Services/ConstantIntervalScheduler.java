package impl.services.Scheduler_Service.Scheduling_Services;

import api.events.EventSource;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.utils.EventBuilder;
import impl.events.EventSourceImpl;
import impl.services.AbstractService;
import impl.services.Scheduler_Service.SchedulerService;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import java.text.ParseException;

/**
 * Created by eric on 7/28/16.
 */
public class ConstantIntervalScheduler extends AbstractService{
    public static final String FORMAT = "ConstantInterval";
    private final AbstractSchedulingService delegateScheduler;
    private Scheduler theScheduler;
    private final EventSource eventGenerator = new EventSourceImpl();

    ConstantIntervalScheduler(EventQueueInterface eventQueueInterface){
        super(eventQueueInterface);
        getEventQueueInterface().addEventSource(eventGenerator);
        try {
            theScheduler = StdSchedulerFactory.getDefaultScheduler();
            theScheduler.start();
        } catch(SchedulerException e) {
            log.error("Could not start a Scheduler: ", e);
            EventBuilder.create()
                    .name(this.toString()+"\n"+getClass().toString())
                    .type(SchedulerService.BAD_SCHEDULER)
                    .data(SchedulerService.FORMAT, FORMAT)
                    .data("Details", e.toString())
                    .send(eventGenerator);
        }

        delegateScheduler = new AbstractSchedulingService(eventQueueInterface, FORMAT, theScheduler) {
            @Override
            protected boolean validateArgs(String args){
                return false;
            }

            @Override
            protected Trigger createTrigger(String args) {
                return null;
            }
        };
    }

    public int countActiveJobs(){
        return delegateScheduler.countActiveJobs();
    }
}
