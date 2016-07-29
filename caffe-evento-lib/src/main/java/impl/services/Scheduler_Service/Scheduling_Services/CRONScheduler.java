package impl.services.Scheduler_Service.Scheduling_Services;

import api.events.Event;
import api.events.EventHandler;
import api.events.EventSink;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.quartz.JobBuilder.newJob;

/**
 * Created by eric on 7/28/16.
 */
public class CRONScheduler extends AbstractService{
    public static final String FORMAT = "CRON";
    private final AbstractSchedulingService delegateScheduler;
    private Scheduler theScheduler;
    private final EventSource eventGenerator = new EventSourceImpl();

    CRONScheduler(EventQueueInterface eventQueueInterface){
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
                return CronExpression.isValidExpression(args);
            }

            @Override
            protected Trigger createTrigger(String args) {
                CronTriggerImpl cronTrigger = new CronTriggerImpl();
                try {
                    cronTrigger.setCronExpression(args);
                }catch(ParseException e){
                    log.error(e);
                }
                return cronTrigger;
            }
        };
    }

    public int countActiveJobs(){
        return delegateScheduler.countActiveJobs();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        theScheduler.shutdown(true);
    }
}