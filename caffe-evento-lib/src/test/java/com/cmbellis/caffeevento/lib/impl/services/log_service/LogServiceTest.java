package com.cmbellis.caffeevento.lib.impl.services.log_service;

import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.events.EventSource;
import com.cmbellis.caffeevento.lib.api.events.event_queue.EventQueue;
import com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface.EventQueueInterface;
import com.cmbellis.caffeevento.lib.api.utils.EventBuilder;
import com.cmbellis.caffeevento.lib.impl.events.EventSourceImpl;
import com.cmbellis.caffeevento.lib.impl.events.event_queue.SynchronousEventQueue;
import com.cmbellis.caffeevento.lib.impl.events.event_queue.event_queue_interface.EventQueueInterfaceImpl;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import com.cmbellis.caffeevento.lib.test_util.EventCollector;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.easymock.EasyMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.*;

/**
 * Created by chris on 7/31/16.
 */
@RunWith(PowerMockRunner.class)
public class LogServiceTest {
    @Mock
    private Log log = createMock(Log.class);
    private EventQueue eventQueue = new SynchronousEventQueue();
    private EventQueueInterface eventQueueInterface = new EventQueueInterfaceImpl();
    private LogService instance = new LogService(eventQueueInterface, log);
    private EventCollector eventCollector = new EventCollector();
    private EventSource eventInjector = new EventSourceImpl();

    @Before
    public void beforeLogTest() {
        eventQueue.addEventQueueInterface(instance.getEventQueueInterface());
        eventQueue.addEventHandler(eventCollector.getHandler());
        eventQueue.addEventSource(eventInjector);
    }

    @Test
    public void testLogError() {
        String message = "Log this Error";
        log.error(message);
        expectLastCall();
        replayAll();
        EventBuilder.create()
                .name("Test Log type:ERROR")
                .data("MESSAGE", message)
                .data("LOG_LEVEL","ERROR")
                .type("LOG")
                .send(eventInjector);
        verifyAll();
    }

    @Test
    public void testLogInfo(){
        String message = "Log this INFO";
        log.info(message);
        expectLastCall();
        replayAll();
        EventBuilder.create()
                .name("Test Log type:INFO")
                .data("MESSAGE", message)
                .data("LOG_LEVEL","INFO")
                .type("LOG")
                .send(eventInjector);
        verifyAll();
    }

    @Test
    public void testLogWarn() {
        String message = "Log this WARN";
        log.warn(message);
        expectLastCall();
        replayAll();
        EventBuilder.create()
                .name("Test Log type:WARN")
                .data("MESSAGE", message)
                .data("LOG_LEVEL","WARN")
                .type("LOG")
                .send(eventInjector);
        verifyAll();
    }

    @Test
    public void testLogDebug(){
        String message = "Log this DEBUG";
        log.debug(message);
        expectLastCall();
        replayAll();
        EventBuilder.create()
                .name("Test Log type:DEBUG")
                .data("MESSAGE", message)
                .data("LOG_LEVEL","DEBUG")
                .type("LOG")
                .send(eventInjector);
        verifyAll();
    }

    @Test
    public void testLogMalformedLogLevel() {
        String message = "Log this XD";
        log.error("Gave an incorrect type for log: " + message);
        expectLastCall();
        replayAll();
        EventBuilder.create()
                .name("Test Log type:XD")
                .data("MESSAGE", message)
                .data("LOG_LEVEL","XD")
                .type("LOG")
                .send(eventInjector);
        verifyAll();
    }

    @Test
    public void testLogAll() {
        List<Event> message =
                Stream.generate(()->EventBuilder.create()
                        .name("Junk Event")
                        .type("junk")
                        .build())
                        .limit(5)
                .collect(Collectors.toList());
        EventBuilder.create()
                .name("Start Log")
                .type("ENABLE_LOG_ALL")
                .send(eventInjector);
        message.forEach(event -> {
            log.info(event.encodeEvent());
            expectLastCall();
        });
        replayAll();
        message.forEach(eventInjector::registerEvent);
        verifyAll();
    }

    @Test
    public void testDisableLogAll(){
        Event unloggableMessage =  EventBuilder.create()
                .name("Unloggable message")
                .type("Spam mail")
                .build();
        List<Event> loggableMessages =
                Stream.generate(()->
                        EventBuilder.create()
                                .name("loggable message")
                                .type("junk mail")
                                .build())
                        .limit(5)
                        .collect(Collectors.toList());
        loggableMessages.forEach(event -> {
            log.info(event.encodeEvent());
            expectLastCall();
        });
        Event disableLogging = EventBuilder.create()
                .name("Stop Log")
                .type("DISABLE_LOG_ALL")
                .build();
        log.info(disableLogging.encodeEvent());
        EventBuilder.create()
                .name("Start Log")
                .type("ENABLE_LOG_ALL")
                .send(eventInjector);
        expectLastCall().anyTimes();
        replayAll();
        loggableMessages.forEach(eventInjector::registerEvent);
        eventInjector.registerEvent(disableLogging);
        eventInjector.registerEvent(unloggableMessage);
        verifyAll();
    }
}