package impl.services.scheduler_service.schedulers;

import api.events.Event;
import api.events.EventHandler;
import api.events.EventSource;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.utils.EventBuilder;
import impl.events.EventSourceImpl;
import impl.services.AbstractService;
import impl.services.scheduler_service.Schedules;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static impl.services.scheduler_service.Schedules.*;

/**
 * Created by eric on 7/28/16.
 */
abstract public class AbstractScheduler extends AbstractService {
    private final String format;

    protected final EventSource eventGenerator = new EventSourceImpl();
    private Map<String, Schedule> activeJobs = new ConcurrentHashMap<>();

    abstract protected boolean validateArgs(String args);
    abstract protected Schedule scheduleJob(String args, String action, String id);

    public AbstractScheduler(EventQueueInterface eventQueueInterface, String format) {
        super(eventQueueInterface);
        this.format = format;
        getEventQueueInterface().addEventSource(eventGenerator);
        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventData(FORMAT, this.format)
                .hasDataKey(ARGS)
                .hasDataKey(SCHEDULED_ACTION)
                .hasDataKey(SCHEDULER_ID_FIELD)
                .eventHandler(event -> {
                    if(validateArgs(event.getEventField(ARGS)) &&
                            Event.decodeEvent(event.getEventField(SCHEDULED_ACTION))
                                    .isPresent()){
                        try {
                            String id = event.getEventField(SCHEDULER_ID_FIELD);
                            Schedule schedule = scheduleJob(event.getEventField(ARGS),
                                    event.getEventField(SCHEDULED_ACTION),
                                    id);
                            schedule.startJob();
                            activeJobs.put(id, schedule);
                            getEventQueueInterface().addEventHandler(activeJobs.get(id).getCancelHandler());
                        }catch(CESchedulerException e){
                            log.error(e);
                            couldNotSchedule(event, "Problem with scheduling: ")
                                    .data("ErrorMessage", e.toString())
                                    .send(eventGenerator);
                        }
                    } else {
                        Schedules.couldNotSchedule(event, "Could not schedule; " +
                                "Invalid ARGS or malformed action: ")
                                .send(eventGenerator);
                    }
                }).build());
    }

    public EventBuilder createScheduleEvent(String args, Event action, UUID schedulerId) {
        return EventBuilder.create()
                .type(SCHEDULE_EVENT)
                .data(FORMAT, this.format)
                .data(ARGS, args)
                .data(SCHEDULED_ACTION, action.encodeEvent())
                .data(SCHEDULER_ID_FIELD, schedulerId.toString());
    }

    public static EventBuilder createScheduleCancelEvent(UUID schedulerId) {
        return EventBuilder.create()
                .type(UNSCHEDULE_EVENT)
                .data(SCHEDULER_ID_FIELD, schedulerId.toString());
    }

    public int countActiveJobs() {
        return activeJobs.size();
    }

    protected class CESchedulerException extends RuntimeException {
        CESchedulerException(String message) {
            super(message);
        }
    }

    protected abstract class Schedule {
        final EventHandler cancel;
        final String id;

        public EventHandler getCancelHandler() {
            return cancel;
        }

        protected abstract void startJob();
        protected abstract void cancelJob();

        Schedule(String Id) {
            id = Id;
            // Create a handler to cancel the event
            cancel = EventHandler.create()
                    .eventType(UNSCHEDULE_EVENT)
                    .eventData(SCHEDULER_ID_FIELD, id)
                    .eventHandler(event -> {
                        this.cancelJob();
                        getEventQueueInterface().removeEventHandler(activeJobs
                                .get(id)
                                .getCancelHandler()
                                .getEventHandlerId());
                        activeJobs.remove(id);
                        EventBuilder.create()
                                .type(UNSCHEDULED_ACTION)
                                .name("Unscheduled" + id)
                                .data(SCHEDULER_ID_FIELD, id)
                                .send(eventGenerator);
                    }).build();
        }
    }
}
