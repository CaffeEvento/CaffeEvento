package impl.services.scheduler_service;

import api.event_queue.Event;
import api.event_queue.EventHandler;
import api.event_queue.EventQueueInterface;
import api.event_queue.EventSource;
import api.utils.EventBuilder;
import impl.event_queue.EventImpl;
import impl.event_queue.EventSourceImpl;
import impl.services.AbstractService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This service takes a schedule event and generates scheduled events
 * Scheduled events happen once at a specific time
 * if the specified execution time has already passed then the scheduled event is triggered immediately
 * Created by eric on 7/15/16.
 */

// TODO: Define DATE_FORMAT Somewhere more public
// TODO: Add ability for ScheduledEvent to occur after a specific delay
// TODO: Add ability for ScheduledEvent to occur more than once (periodically)

public class SchedulerService extends AbstractService {
    public static final String DATE_FORMAT = "dow mon dd hh:mm:ss zzz yyyy";

    /* Schedule Event Types */
    public static final String SCHEDULE_EVENT_TYPE = "SCHEDULE";
    public static final String SCHEDULE_EVENT_CANCEL_TYPE = "UNSCHEDULE";

    /* Schedule Fields */
    public static final String SCHEDULED_EVENT_ACTION = "SCHEDULED_ACTION";
    public static final String SCHEDULE_ID_FIELD = "SCHEDULER_ID";

    /* (soon to be) Optional Fields */
    //sets the time of the first occurence of ScheduledEvent
    public static final String SCHEDULER_TIME_FIELD = "SCHEDULED_TIME";
    //sets minimum time in milliseconds to first instance from when SchedulerService recieves the Event, overrides ScheduledTime if later
    public static final String SCHEDULER_DELAY_FIELD = "DELAY";
    //sets time at which the last ScheduledEvent may occur. If this is after the current time then the ScheduledEvent never happens.
    public static final String SCHEDULER_END_TIME_FIELD = "SCHEDULED_END_TIME";
    //sets maximum time during which ScheduledEvent can repeat after first occurence, overrides Scheduled end time if shorter
    public static final String SCHEDULER_MAXDURATION_FIELD = "MAX_DURATION";
    //sets the period between event recurrences, if not specified the ScheduledEvent does not repeat
    public static final String SCHEDULER_REPEAT_PERIOD = "PERIOD";
    //sets the maximum number of times which an event can repeat, overrides scheduled end time and max duration if fewer
    public static final String SCHEDULER_REPEAT_LIMIT = "REPEATED_EVENT_LIMIT";
    //gives the number of times which the event will repeat, overrides scheduler repeat limit if fewer.
    public static final String SCHEDULER_REPEATS = "REPEATS";

    /* (optional) Fields Added to ScheduledEvent */
    public static final String SCHEDULED_EVENT_ITERATION = "SCHEDULED_EVENT_ITERATION";

    private static final Log log = LogFactory.getLog(SchedulerService.class);

    private final EventSource eventGenerator = new EventSourceImpl();
    private final Map<UUID, Scheduler> activeSchedulers= new HashMap<>();

    public SchedulerService(EventQueueInterface eventQueueInterface)
    {
        super(eventQueueInterface);
        getEventQueueInterface().addEventSource(eventGenerator);

        // Add the Schedule event handler
        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventType(SCHEDULE_EVENT_TYPE)
                .eventHandler(theEvent -> {
                    try {
                        Scheduler theScheduler = new Scheduler(theEvent);
                        activeSchedulers.put(theScheduler.getSchedulerId(), theScheduler);
                    } catch (SchedulerException e) {
                        log.error("could not schedule a timer for the event.", e);
                        e.printStackTrace();
                    }
                }).build());
    }

    public static Event generateSchedulerEvent(String eventName, Event actionEvent) {
        return EventBuilder.create()
                .name(eventName).type(SCHEDULE_EVENT_TYPE)
                .data(SCHEDULED_EVENT_ACTION, actionEvent.encodeEvent())
                .data(SCHEDULE_ID_FIELD, UUID.randomUUID().toString())
                .build();
    }

    public static Event generateSchedulerCancelEvent(String eventName, UUID schedulerId) {
        return EventBuilder.create()
                .name(eventName)
                .type(SCHEDULE_EVENT_CANCEL_TYPE)
                .data(SCHEDULE_ID_FIELD, schedulerId.toString()).build();
    }

    public int numberOfActiveSchedulers() {
        return activeSchedulers.size();
    }

    private class Scheduler {
        private final UUID schedulerId;
        private List<EventHandler> SchedulerEventHandlers =  new ArrayList<>();
        private Timer eventTimer = new Timer();
        private Date scheduledTime;

        public Scheduler(Event sourceEvent) throws SchedulerException {
            if (sourceEvent.getEventField(SCHEDULE_ID_FIELD) == null) {
                throw new SchedulerException("No Scheduler ID field.");
            }
            if (sourceEvent.getEventField(SCHEDULER_TIME_FIELD) == null) {
                throw new SchedulerException("No time specified.");
            }
            try {
                scheduledTime = new SimpleDateFormat(DATE_FORMAT).parse(sourceEvent.getEventField(SCHEDULER_TIME_FIELD));
            } catch(ParseException e) {
                throw new SchedulerException("Invalid DATE_FORMAT");
            }

            schedulerId = UUID.fromString(sourceEvent.getEventField(SCHEDULE_ID_FIELD));
            Event scheduledEvent = Event.decodeEvent(sourceEvent.getEventField(SCHEDULED_EVENT_ACTION));
            scheduledEvent.setEventField(SCHEDULE_ID_FIELD, schedulerId.toString());

            // add the canceled request handler
            EventHandler canceled = EventHandler.create()
                    .eventType(SCHEDULE_EVENT_CANCEL_TYPE)
                    .eventData(SCHEDULE_ID_FIELD, schedulerId.toString())
                    .eventHandler(event -> {
                        eventTimer.cancel(); // this line actually stops the ScheduledEvent, does nothing if already canceled, can be used to check success
                        eventGenerator.registerEvent(createSchedulerCanceledEvent());
                        SchedulerEventHandlers.forEach(e -> getEventQueueInterface().removeEventHandler(e));
                        activeSchedulers.remove(schedulerId);
                    }).build();

            SchedulerEventHandlers.add(canceled);
            getEventQueueInterface().addEventHandler(canceled);
            // this part only allows the ScheduledEvent to occur once, modify this to allow repeated events
            eventTimer.schedule(new TimerTask() {
                public void run() {
                    eventGenerator.registerEvent(scheduledEvent);
                    /* Final Iteration */
                    // Unregister the canceled EventHandler upon final execution
                    SchedulerEventHandlers.forEach(e -> getEventQueueInterface().removeEventHandler(e));
                    // remove the scheduler from active schedulers when it completes.
                    activeSchedulers.remove(schedulerId);
                    // stop timer after final execution.
                    eventTimer.cancel();
                }
            }, scheduledTime);
        }

        private Event createSchedulerCanceledEvent() {
            Event canceledEvent =  new EventImpl("Canceled Scheduler " + schedulerId, SCHEDULE_EVENT_CANCEL_TYPE);
            canceledEvent.setEventField(SCHEDULE_ID_FIELD, schedulerId.toString());
            return canceledEvent;
        }

        public UUID getSchedulerId(){
            return schedulerId;
        }
    }
}