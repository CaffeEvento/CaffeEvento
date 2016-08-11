package com.cmbellis.caffeevento.lib.impl.lib.servlet_server;

import com.cmbellis.caffeevento.lib.api.lib.ServerHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chris on 7/27/16.
 */
public final class SimpleServletRegister {
    private Map<String, ServerHandler> handlers = new HashMap<>();
    private Log log = LogFactory.getLog(getClass());

    public SimpleServletRegister(ServletContextHandler contextHandler) {
        contextHandler.addServlet(new ServletHolder(new HandlerServlet()), "/*");
    }

    public void addServlet(String path, ServerHandler handler) {
        handlers.put(path, handler);
    }

    private class HandlerServlet extends HttpServlet {
        @Override
        public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
            String requestedPath = req.getPathInfo();
            ServerHandler handler = handlers.get(requestedPath);
            if (handler != null) {
                handler.processRequest(req, res);
            } else {
                log.error("No handler for path: " + requestedPath);
                res.setStatus(404);
                res.getWriter().println("No handler for path: " + requestedPath);
            }
        }
    }
}
