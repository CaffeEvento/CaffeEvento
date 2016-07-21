package server;

import api.lib.EmbeddedServletServer;
import impl.lib.servlet_server.EmbeddedServletServerImpl;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import java.util.UUID;

/**
 * Created by chris on 7/18/16.
 */
public class Main {
    private static Server server;

    public static void main(String[] args) throws Exception{
        server = new Server(2345);
        setupRemoteServerService(server);
        server.start();
    }

    private static void setupRemoteServerService(Server server) throws Exception{
        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/servers");
        EmbeddedServletServer servletServer = new EmbeddedServletServerImpl(servletContextHandler);

        UUID serviceId = UUID.randomUUID();
        String serviceName = "My Service";
        servletServer.addService(serviceName, serviceId, "/test", (req, res) -> res.getWriter().write("This was a testy"));
        servletServer.addService(serviceName, serviceId, "/test2", (req, res) -> res.getWriter().write("Making a note here, huge success"));
        servletServer.addService(serviceName, serviceId, "/test3", (req, res) -> res.getWriter().write("Its hard to overstate my satisfaction"));
        servletServer.addService(serviceName, serviceId, "/test4", (req, res) -> res.getWriter().write("Aperture science.. we do what we must"));

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setResourceBase(".");
        ContextHandler resourceContextHandler = new ContextHandler();
        resourceContextHandler.setContextPath("/*");
        resourceContextHandler.setHandler(resource_handler);

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resourceContextHandler, servletContextHandler, new DefaultHandler() });
        server.setHandler(handlers);
    }
}
