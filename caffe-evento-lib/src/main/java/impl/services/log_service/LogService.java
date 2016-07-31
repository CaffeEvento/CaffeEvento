package impl.services.log_service;

import api.events.EventHandler;
import api.events.event_queue.event_queue_interface.EventQueueInterface;
import api.events.EventSource;
import impl.events.EventSourceImpl;
import impl.services.AbstractService;

/** TODO:Pair this Service with a logging utils
 * Created by eric on 7/20/16.
 */

//TODO: Standardize how EventQueues handle unhandled events (defaultHandler)[probably do so in Utils.logger rather than here]
public class LogService extends AbstractService{
    private EventSource eventGenerator =  new EventSourceImpl();
    LogService(EventQueueInterface eventQueueInterface) {
        super(eventQueueInterface);
        getEventQueueInterface().addEventSource(eventGenerator);

        //TODO: Decide if the logging procedure may be configured by events.
        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventHandler(theEvent -> {
                    // TODO: Set up a size limited queue/logger that records the latest N events to pass the event handler.
                }).build()
        );

        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventType("LOG")
                .eventHandler(theEvent -> {
                    //TODO:Figure out how to log events of type:LOG
                    //log the event
                }).build()
        );

        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventType("ERROR")
                .eventHandler(theEvent -> {
                    //TODO:Figure out how to log events of type:ERROR, should probably include a list of the latest events as well.
                    //log with special treatment
                }).build()
        );

        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventType("GET_LOG")
                .eventHandler(theEvent -> {
                    //TODO: Return the log when requested
                }).build()
        );

        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventType("GET_ERROR")
                .eventHandler(theEvent -> {
                    //TODO: Return the log of all recent errors when requested, limit errors to predefined amount
                }).build()
        );

        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventType("GET_RECENT")
                .eventHandler(theEvent -> {
                    //TODO: Return all recent events to occur, limit to specified size
                }).build()
        );
    }
}
