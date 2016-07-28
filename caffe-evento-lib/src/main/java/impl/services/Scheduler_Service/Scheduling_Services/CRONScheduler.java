package impl.services.Scheduler_Service.Scheduling_Services;

import api.events.EventHandler;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import impl.services.AbstractService;
import impl.services.Scheduler_Service.SchedulerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Created by eric on 7/28/16.
 */
public class CRONScheduler extends AbstractService{
    public static final String CRON_regex = null;
    Log log = LogFactory.getLog(getClass());
    private Scheduler scheduler;
    CRONScheduler(EventQueueInterface eventQueueInterface) {
        super(eventQueueInterface);
        try{
            scheduler = StdSchedulerFactory.getDefaultScheduler();
        }catch(SchedulerException e){
            log.error("Could not create a Scheduler: ", e);
        }

        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventDataLike(SchedulerService.ARGS, CRON_regex)
                .eventHandler(event -> {
                    //Schedule the job here
                })
                .build()
        );
    }

}
