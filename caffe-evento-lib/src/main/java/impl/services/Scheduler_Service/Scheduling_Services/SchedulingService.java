package impl.services.Scheduler_Service.Scheduling_Services;

import api.events.EventHandler;
import api.events.EventSource;
import api.events.event_queue.event_queue_interface.EventQueueInterface;

import java.util.List;

/**
 * Created by eric on 7/28/16.
 */
public interface SchedulingService {
    List<EventHandler> getSchedulingHandlers();
    List<EventSource> getEventSources();
    EventQueueInterface getSchedulingEventQueueInterface();
    EventQueueInterface getScheduledEventQueueInterface();
}