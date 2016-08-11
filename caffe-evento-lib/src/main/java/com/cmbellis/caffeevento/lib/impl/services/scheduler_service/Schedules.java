package com.cmbellis.caffeevento.lib.impl.services.scheduler_service;

import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.utils.EventBuilder;

/**
 * Created by eric on 8/9/16.
 */
public interface Schedules {
    /* constants */
        /* mandatory fields */
    public static final String FORMAT = "SCHEDULER_FORMAT";
    public static final String ARGS = "SCHEDULER_ARGS";
    public static final String SCHEDULER_ID_FIELD = "SCHEDULER_ID";
    public static final String SCHEDULED_ACTION = "SCHEDULED_EVENT";
    /* pick one eventType */
    public static final String SCHEDULE_EVENT = "SCHEDULE";
    public static final String UNSCHEDULE_EVENT = "UNSCHEDULE";
    /* pick one reply */
    public static final String SCHEDULER_ERROR = "UNSCHEDULABLE_EVENT";
    public static final String UNSCHEDULED_ACTION = "UNSCHEDULED";
    public static final String BAD_SCHEDULER = "SCHEDULER_FAILURE";

    public static EventBuilder couldNotSchedule(Event event, String reason) {
        return EventBuilder.create()
                .type(SCHEDULER_ERROR)
                .name(reason + event.getEventName())
                .data(SCHEDULER_ID_FIELD, event.getEventField(SCHEDULER_ID_FIELD))
                .data("Details", event.encodeEvent());
    }
}
