package com.cmbellis.caffeevento.lib.impl.services.log_service;

import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.events.EventHandler;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.api.events.EventSource;
import com.cmbellis.caffeevento.lib.impl.events.EventSourceImpl;
import com.cmbellis.caffeevento.lib.impl.services.AbstractService;
import org.apache.commons.logging.Log;

import java.util.Optional;

/**
 * Created by eric on 7/20/16.
 */

public class LogService extends AbstractService{
    private EventSource eventGenerator =  new EventSourceImpl();
    private EventHandler logAllEventHandler = null;
    private Log log;
    public enum LogLevel {
        INFO, WARN, ERROR, DEBUG, NO_TYPE;

        public static LogLevel convert(String s) {
            switch (s) {
                case "INFO":
                    return INFO;
                case "WARN":
                    return WARN;
                case "ERROR":
                    return ERROR;
                case "DEBUG":
                    return DEBUG;
                default:
                    return NO_TYPE;
            }
        }
    }

    LogService(EventQueueInterface eventQueueInterface, Log log) {
        super(eventQueueInterface);
        getEventQueueInterface().addEventSource(eventGenerator);
        this.log = log;

        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventType("ENABLE_LOG_ALL")
                .eventHandler(theEvent -> {
                    Optional.ofNullable(logAllEventHandler).ifPresent(getEventQueueInterface()::removeEventHandler);
                    logAllEventHandler = EventHandler.create().eventHandler(this::logMessage).build();
                    getEventQueueInterface().addEventHandler(logAllEventHandler);
                }).build());

        getEventQueueInterface().addEventHandler(EventHandler.create()
            .eventType("DISABLE_LOG_ALL")
            .eventHandler(theEvent -> {
                Optional.ofNullable(logAllEventHandler).ifPresent(getEventQueueInterface()::removeEventHandler);
                logAllEventHandler = null;
            }).build());


        getEventQueueInterface().addEventHandler(EventHandler.create()
                .eventType("LOG")
                .hasDataKey("LOG_LEVEL")
                .hasDataKey("MESSAGE")
                .eventHandler(theEvent -> {
                    logMessage(LogLevel.convert(theEvent.getEventField("LOG_LEVEL")),
                            theEvent.getEventField("MESSAGE"));
                }).build()
        );
    }

    private void logMessage(Event e) {
        logMessage(LogLevel.INFO, e.encodeEvent());
    }

    private void logMessage(LogLevel level, String message) {
        switch (level) {
            case INFO:
                log.info(message);
                break;
            case WARN:
                log.warn(message);
                break;
            case ERROR:
                log.error(message);
                break;
            case DEBUG:
                log.debug(message);
                break;
            case NO_TYPE:
                log.error("Gave an incorrect type for log: " + message);
                break;
            default:
                log.error("HOW THE FUCK?" + message);
        }
    }
}
