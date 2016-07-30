package impl.services.Scheduler_Service.Scheduling_Services;

import api.events.EventSource;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.utils.EventBuilder;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import impl.events.EventSourceImpl;
import impl.services.AbstractService;
import impl.services.Scheduler_Service.SchedulerService;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import java.util.Date;
import java.util.Optional;

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
                try {
                    return Optional.ofNullable((new GsonBuilder()).create().fromJson(args, Arguments.class)).isPresent();
                } catch (JsonSyntaxException e){
                    return false;
                }
            }

            @Override
            protected Trigger createTrigger(String args) {
                SimpleTriggerImpl trigger = new SimpleTriggerImpl();
                try {
                    Optional.ofNullable((new GsonBuilder()).create().fromJson(args, Arguments.class))
                            .ifPresent(arguments -> {
                                if (arguments.Repeats != null) {
                                    trigger.setTimesTriggered(arguments.Repeats);
                                }
                                if (arguments.Period != null) {
                                    trigger.setRepeatInterval(arguments.Period);
                                }
                                if (arguments.StartTime != null) {
                                    trigger.setStartTime(arguments.StartTime);
                                }
                                if (arguments.EndTime != null) {
                                    trigger.setEndTime(arguments.EndTime);
                                }
                            });
                }catch(JsonSyntaxException e){
                    log.error(e);
                    throw new absScheduleException("Not able to parse args.");
                }
                return trigger;
            }
        };
    }

    public class Arguments{
        public Date StartTime = null;
        public Long Period = null;
        public Date EndTime = null;
        public Integer Repeats = null;
        public String toJson(){
            return (new GsonBuilder()).create().toJson(this);
        }
    }

    public int countActiveJobs(){
        return delegateScheduler.countActiveJobs();
    }
}
