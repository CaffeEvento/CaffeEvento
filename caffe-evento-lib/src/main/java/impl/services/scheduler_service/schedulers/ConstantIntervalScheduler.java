package impl.services.scheduler_service.schedulers;

import api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.quartz.*;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import java.util.Date;
import java.util.Optional;

/**
 * Created by eric on 7/28/16.
 */
public class ConstantIntervalScheduler extends AbstractScheduler {
    public static final String format = "ConstantInterval";

    ConstantIntervalScheduler(EventQueueInterface eventQueueInterface, Scheduler scheduler){
        super(eventQueueInterface, format, scheduler);
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
        } catch(JsonSyntaxException e){
            log.error(e);
            throw new CESchedulerException("Unable to parse args.");
        }
        return trigger;
    }

    @Override
    protected boolean validateArgs(String args) {
        try {
            return Optional.ofNullable((new GsonBuilder()).create().fromJson(args, Arguments.class)).isPresent();
        } catch (JsonSyntaxException e){
            return false;
        }
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
}
