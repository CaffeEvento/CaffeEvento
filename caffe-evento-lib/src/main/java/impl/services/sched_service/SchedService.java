package impl.services.sched_service;

import api.events.Event;
import api.events.EventHandler;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.events.EventSource;
import api.utils.EventBuilder;
import impl.events.EventImpl;
import impl.events.EventSourceImpl;
import impl.services.AbstractService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

/**
 * This service takes a schedule event and generates scheduled events
 * Scheduled events happen once at a specific time
 * if the specified execution time has already passed then the scheduled event is triggered immediately
 * Created by eric on 7/15/16.
 */

// TODO: Define DATE_FORMAT Somewhere more public
// TODO: Add ability to limit the maximum number of times that ScheduledEvent occurs when repeating event

public class SchedService extends AbstractService {
    public static final String DATE_FORMAT = "EEE MMM dd hh:mm:ss zzz yyyy";

    /* Schedule Event Types */
    public static final String SCHEDULE_EVENT_TYPE = "SCHEDULE";
    public static final String SCHEDULE_EVENT_CANCEL_TYPE = "UNSCHEDULE";
    public static final String SCHEDULE_EVENT_CANCELED = "ACTION_UNSCHEDULED";

    /* Schedule Fields */
    // Mandatory field in SCHEDULE event types describing the action to take
    public static final String SCHEDULED_EVENT_ACTION = "SCHEDULED_ACTION";
    // Field used only in UNSCHEDULE event types
    public static final String SCHEDULE_ID_FIELD = "SCHEDULER_ID";

    /* Optional Fields */
    //sets the time of the first occurence of ScheduledEvent, if not present then ScheduledEvent is Scheduled to occur immediately
    public static final String START_TIME = "SCHEDULED_TIME";
    //sets minimum time to first instance from when SchedService recieves the Event, overrides ScheduledTime if later.
    public static final String DELAY = "DELAY";
    //sets the period between event recurrences, if not specified the ScheduledEvent does not repeat
    public static final String REPEAT_PERIOD = "PERIOD";
    //sets time at which the last ScheduledEvent may occur. If this is after the current time then the ScheduledEvent never happens.
    public static final String END_TIME = "SCHEDULED_END_TIME";
    //sets maximum time during which ScheduledEvent can repeat after first occurence, overrides Scheduled end time if shorter
    public static final String MAXDURATION = "MAX_DURATION";
    //sets maximum number of times ScheduledEvent will be allowed to occur when scheduled as a repeating event
    public static final String MAXREPEATS = "MAX_REPEATS";

    /* (optional) Fields Added to ScheduledEvent */
    public static final String SCHEDULED_EVENT_ITERATION = "SCHEDULED_EVENT_ITERATION";

    private static final Log log = LogFactory.getLog(SchedService.class);

    private final EventSource eventGenerator = new EventSourceImpl();
    private final Map<UUID, Scheduler> activeSchedulers= new ConcurrentHashMap<>();
    private final Clock clock;
    private final ScheduledExecutorService eventTimer;

    public SchedService(EventQueueInterface eventQueueInterface) {
        this(eventQueueInterface, Clock.systemUTC());
    }

    public SchedService(EventQueueInterface eventQueueInterface, Clock clock) {
        this(eventQueueInterface, clock, Executors.newScheduledThreadPool(1));
    }

    public SchedService(EventQueueInterface eventQueueInterface, Clock clock, ScheduledExecutorService executorService)
    {
        super(eventQueueInterface);
        this.clock = clock;
        this.eventTimer = executorService;
        getEventQueueInterface().addEventSource(eventGenerator);

        // Add the Schedule event handler
        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventType(SCHEDULE_EVENT_TYPE)
                .eventHandler(theEvent -> {
                    try {
                        Scheduler theScheduler = new Scheduler(theEvent);
                        activeSchedulers.put(theScheduler.getSchedulerId(), theScheduler);
                    } catch (SchedException e) {
                        log.error("could not schedule a timer for the event.", e);
                        e.printStackTrace();
                    }
                }).build());
    }

    public static Event generateSchedulerEvent(String eventName, Event actionEvent, Map<String, String> arguments) {
        EventBuilder schedulerBuilder = EventBuilder.create();
        schedulerBuilder.name(eventName)
                .type(SCHEDULE_EVENT_TYPE)
                .data(SCHEDULE_ID_FIELD, UUID.randomUUID().toString())
                .data(SCHEDULED_EVENT_ACTION, actionEvent.encodeEvent());
        arguments.forEach(schedulerBuilder::data);
        return schedulerBuilder.build();
    }

    public static Event generateSchedulerCancelEvent(String eventName, UUID schedulerId) {
        return EventBuilder.create()
                .name(eventName)
                .type(SCHEDULE_EVENT_CANCEL_TYPE)
                .data(SCHEDULE_ID_FIELD, schedulerId.toString())
                .build();
    }

    public int numberOfActiveSchedulers() {
        return activeSchedulers.size();
    }

    private class Scheduler {
        private final UUID schedulerId;
        //TODO: Prevent concurrent modification of SchedulerEventHandlers
        private List<EventHandler> SchedulerEventHandlers = new ArrayList<>();
        private Event scheduledEvent;

        public Scheduler(Event sourceEvent) throws SchedException {
            final ScheduledFuture<?> fireScheduledEventHandle;
            long delay = Long.MIN_VALUE;
            long maxDelayToFinish = Long.MAX_VALUE;
            long period = Long.MAX_VALUE;

            if (sourceEvent.getEventField(SCHEDULE_ID_FIELD) == null) {
                throw new SchedException("No Scheduler ID field.");
            }
            if (sourceEvent.getEventField(SCHEDULED_EVENT_ACTION) == null) {
                throw new SchedException("No Event to Schedule");
            }

            try {
                this.schedulerId = UUID.fromString(sourceEvent.getEventField(SCHEDULE_ID_FIELD));
            } catch(IllegalArgumentException e) {
                throw new SchedException("Recieved invalid Scheduler ID field, unable to convert to UUID: " + sourceEvent.getEventField(SCHEDULE_ID_FIELD));
            }

            scheduledEvent = Event.decodeEvent(sourceEvent.getEventField(SCHEDULED_EVENT_ACTION))
                    .orElseThrow(()-> new SchedException("Malformatted Event to Schedule"));

            // break out all the optional field
            try {
                if (sourceEvent.getEventField(END_TIME) != null) {
                    maxDelayToFinish = Duration.between(Instant.now(clock), (new SimpleDateFormat(DATE_FORMAT).parse(sourceEvent.getEventField(END_TIME))).toInstant()).toMillis();
                }
                if (sourceEvent.getEventField(START_TIME) != null) {
                    delay = Duration.between(Instant.now(clock), (new SimpleDateFormat(DATE_FORMAT).parse(sourceEvent.getEventField(START_TIME))).toInstant()).toMillis();
                }
                if (sourceEvent.getEventField(DELAY) != null) {
                    delay = Long.max(Duration.parse(sourceEvent.getEventField(DELAY)).toMillis(), delay);
                }
                if (sourceEvent.getEventField(MAXDURATION) != null) {
                    maxDelayToFinish = Long.min(Duration.parse(sourceEvent.getEventField(MAXDURATION)).toMillis() + delay, maxDelayToFinish);
                }
                if (sourceEvent.getEventField(REPEAT_PERIOD) != null) {
                    period =  Duration.parse(sourceEvent.getEventField(REPEAT_PERIOD)).toMillis();
                }
            } catch (ParseException e) {
                throw new SchedException("Could not parse Absolute Time Field");
            } catch (DateTimeParseException e) {
                throw new SchedException("Could not parse Duration Field");
            }

            // if the event can still happen schedule the event to occur
            if (maxDelayToFinish > delay) {
                if (sourceEvent.getEventField(REPEAT_PERIOD) != null) {
                    // this launches the repeating event
                    try {
                        fireScheduledEventHandle =
                                eventTimer.scheduleAtFixedRate(
                                        () -> eventGenerator.registerEvent(new EventImpl(scheduledEvent)),
                                        delay,
                                        period,
                                        TimeUnit.MILLISECONDS
                                );
                    }catch(IllegalArgumentException e){
                        throw new SchedException("Repeat Period invalid: " + Duration.of(period, ChronoUnit.MILLIS).toString() + "\nShould be: " + sourceEvent.getEventField(REPEAT_PERIOD));
                    }
                } else {
                    // this launches on a non-repeating event
                    fireScheduledEventHandle =
                            eventTimer.schedule(
                                    ()->{
                                        eventGenerator.registerEvent(new EventImpl(scheduledEvent));
                                        SchedulerEventHandlers.forEach(e -> getEventQueueInterface().removeEventHandler(e));
                                        activeSchedulers.remove(schedulerId);
                                    },
                                    delay,
                                    TimeUnit.MILLISECONDS
                            );
                }

                // add the canceled request handler
                EventHandler canceled = EventHandler.create()
                        .eventType(impl.services.sched_service.SchedService.SCHEDULE_EVENT_CANCEL_TYPE)
                        .eventData(impl.services.sched_service.SchedService.SCHEDULE_ID_FIELD, schedulerId.toString())
                        .eventHandler(event -> {
                            fireScheduledEventHandle.cancel(true); // this line actually stops the ScheduledEvent
                            createSchedulerCanceledEvent().send(eventGenerator);
                            SchedulerEventHandlers.forEach(e -> getEventQueueInterface().removeEventHandler(e));
                            activeSchedulers.remove(schedulerId);
                        }).build();
                SchedulerEventHandlers.add(canceled);
                getEventQueueInterface().addEventHandler(canceled);

                //the stop timer is only supposed to run if one of the time limiting fields is set.
                if ((sourceEvent.getEventField(END_TIME) != null) || (sourceEvent.getEventField(MAXDURATION) != null)) {
                    // this launches the stop timer
                    eventTimer.schedule(
                            ()-> {
                                SchedulerEventHandlers.forEach(e -> getEventQueueInterface().removeEventHandler(e));
                                activeSchedulers.remove(schedulerId);
                                fireScheduledEventHandle.cancel(true);
                            },
                            maxDelayToFinish,
                            TimeUnit.MILLISECONDS
                    );
                }
            }
        }

        private EventBuilder createSchedulerCanceledEvent() {
            return EventBuilder.create()
                    .name("Canceled Scheduler " + schedulerId)
                    .type(SCHEDULE_EVENT_CANCELED)
                    .data(SCHEDULE_ID_FIELD, schedulerId.toString());
        }

        public UUID getSchedulerId() {
            return schedulerId;
        }
    }
}
