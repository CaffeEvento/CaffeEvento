package impl.services.Scheduler_Service;

import api.events.EventSource;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import impl.events.EventSourceImpl;
import impl.services.AbstractService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by eric on 7/22/16.
 */
public class SchedulerService extends AbstractService {
    /* constants */
        /* mandatory fields */
        private static final String FORMAT = "SCHEDULER_FORMAT";
        private static final String ARGS = "SCHEDULER_ARGS";
        private static final String SCHEDULER_ID_FIELD = "SCHEDULER_ID";
        private static final String SCHEDULED_EVENT = "SCHEDULED_EVENT";

    /* finals */
        private static final Log log = LogFactory.getLog(SchedulerService.class);
        private final EventSource eventGenerator = new EventSourceImpl();

    /* code */
    SchedulerService(EventQueueInterface eventQueueInterface){
        super(eventQueueInterface);

    }

    private class schedulerJob implements Job {
        public schedulerJob(){}
        public void execute(JobExecutionContext context)
                throws JobExecutionException {
            //This part does the thing
        }
    }
}
