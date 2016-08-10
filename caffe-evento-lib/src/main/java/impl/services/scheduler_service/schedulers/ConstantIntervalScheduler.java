package impl.services.scheduler_service.schedulers;

import api.events.Event;
import api.events.EventHandler;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.utils.EventBuilder;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import impl.services.scheduler_service.SchedulerContainerService;
import org.quartz.*;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by eric on 7/28/16.
 */
public class ConstantIntervalScheduler extends AbstractQuartzScheduler {
    public static final String format = "ConstantInterval";

    public ConstantIntervalScheduler(EventQueueInterface eventQueueInterface, Scheduler scheduler){
        super(eventQueueInterface, format, scheduler);
    }

    @Override
    protected Trigger createTrigger(String args) {
        SimpleTriggerImpl trigger = new SimpleTriggerImpl();
        try {
            Optional.ofNullable((new GsonBuilder()).create().fromJson(args, Arguments.class))
                    .ifPresent(arguments -> {
                        Optional.ofNullable(arguments.getRepeats()).ifPresent(trigger::setTimesTriggered);
                        Optional.ofNullable(arguments.getPeriod()).ifPresent(trigger::setRepeatInterval);
                        Optional.ofNullable(arguments.getStartTime()).ifPresent(trigger::setStartTime);
                        Optional.ofNullable(arguments.getEndTime()).ifPresent(trigger::setEndTime);
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
        private Date startTime = null;
        private Long period = null;
        private Date endTime = null;
        private Integer repeats = null;
        public Arguments() {}
        public Arguments(Date startTime, Long period, Date endTime, Integer repeats) {
            this.startTime = startTime;
            this.period = period;
            this.endTime = endTime;
            this.repeats = repeats;
        }
        public String toJson(){
            return (new GsonBuilder()).create().toJson(this);
        }
        public Date getStartTime() {
            return startTime;
        }

        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }

        public Long getPeriod() {
            return period;
        }

        public void setPeriod(long period) {
            if (period >= 0) {
                this.period = period;
            } else {
                throw new IllegalArgumentException("Period cannot be negative!");
            }
        }

        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(Date endTime) {
            this.endTime = endTime;
        }

        public Integer getRepeats() {
            return repeats;
        }

        public void setRepeats(int repeats) {
            if (repeats > 0) {
                this.repeats = repeats;
            } else {
                throw new IllegalArgumentException("Repeats shan't be zero or less than zero!");
            }
        }
    }
}
