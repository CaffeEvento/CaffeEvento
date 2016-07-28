package server;


import api.events.Event;
import api.events.EventSource;
import api.events.event_queue.EventQueue;
import api.services.Service;
import impl.events.EventSourceImpl;
import impl.events.event_queue.AsynchronousEventQueue;
import impl.lib.servlet_server.SimpleServletRegister;
import impl.services.remote_service.RemoteServerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by chris on 7/18/16.
 */
public class Main {
    private static Server server;
    private static Log log = LogFactory.getLog(Main.class);
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static EventQueue eventQueue;
    private static EventSource eventInjector = new EventSourceImpl();

    public static void main(String[] args) throws Exception{
        server = new Server(2345);

        // Setup the remote server service
        ServletContextHandler remoteServiceHandler = getServerHandler("/remote_service");
        RemoteServerService remoteServerService = new RemoteServerService("remote_service", remoteServiceHandler);
        ServletContextHandler servletRegisterHandler = getServerHandler("/servlets");
        SimpleServletRegister servletRegister = new SimpleServletRegister(servletRegisterHandler);
        servletRegister.addServlet("/registerEvent", (req, res) -> Event.decodeEvent(req.getReader())
                .ifPresent(eventInjector::registerEvent));

        Handler resourceHandler = getResourceHandler();
        setHandlers(server, remoteServiceHandler, servletRegisterHandler, resourceHandler, new DefaultHandler());
        server.start();
        eventQueue = new AsynchronousEventQueue(executor, e -> log.info("Event queue did not handle event: " + e.encodeEvent()));
        eventQueue.addEventSource(eventInjector);
        eventQueue.registerService(remoteServerService);

        log.info("Services started!");
    }

    private static void setHandlers(Server server, Handler...handlers) {
        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(handlers);
        server.setHandler(handlerList);
    }

    private static ServletContextHandler getServerHandler(String contextPath) throws Exception {
        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(contextPath);
        return servletContextHandler;
    }

    private static Handler getResourceHandler() throws Exception {
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setResourceBase(".");
        ContextHandler resourceContextHandler = new ContextHandler();
        resourceContextHandler.setContextPath("/*");
        resourceContextHandler.setHandler(resource_handler);
        return resourceContextHandler;
    }
}
