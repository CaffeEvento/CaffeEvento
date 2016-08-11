package com.cmbellis.caffeevento.lib.impl.lib.servlet_server;

import com.cmbellis.caffeevento.lib.api.lib.EmbeddedServletServer;
import com.cmbellis.caffeevento.lib.api.lib.ServerHandler;
import com.google.gson.JsonObject;
import com.cmbellis.caffeevento.lib.impl.utils.JSONUtils;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by chris on 7/14/16.
 */
public final class EmbeddedServletServerImpl implements EmbeddedServletServer {
    private Map<String, UUID> serviceNames = new HashMap<>();
    private Map<UUID, String> serviceIds = new HashMap<>();
    private Map<UUID, Map<String, ServerHandler>> handlers = new HashMap<>();

    public EmbeddedServletServerImpl(ServletContextHandler contextHandler) {
        contextHandler.addServlet(new ServletHolder(new GetServicesServlet()), "/services");
        contextHandler.addServlet(new ServletHolder(new ProcessDataServlet()), "/*");
    }

    @Override
    public void addService(String serviceName, UUID serviceId, String path, ServerHandler handler) {
        if (!serviceNames.containsKey(serviceName)) {
            serviceNames.put(serviceName, serviceId);
            serviceIds.put(serviceId, serviceName);
            handlers.put(serviceId, new HashMap<>());
        }
        handlers.get(serviceId).put(path, handler);
    }

    @Override
    public void removeService(String serviceName) {
        Optional<UUID> id = Optional.ofNullable(serviceNames.get(serviceName));
        id.ifPresent(handlers::remove);
        id.ifPresent(serviceIds::remove);
        serviceNames.remove(serviceName);
    }

    @Override
    public void removeService(UUID serviceId) {
        Optional<String> serviceName = Optional.ofNullable(serviceIds.get(serviceId));
        serviceName.ifPresent(serviceNames::remove);
        handlers.remove(serviceId);
        serviceIds.remove(serviceId);
    }

    private Map<String, UUID> getServices() {
        return serviceNames;
    }

    private Map<UUID, String> getServiceIds() {
        return serviceIds;
    }

    private Map<UUID, Map<String, ServerHandler>> getHandlers() {
        return handlers;
    }

    private class GetServicesServlet extends HttpServlet {
        @Override
        public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
            res.getWriter().write(JSONUtils.convertToJson(getServices()));
        }
    }

    private class ProcessDataServlet extends HttpServlet {
        @Override
        public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
            String[] pathInfo = req.getPathInfo().split("/");
            if(pathInfo.length == 0) {
                res.getWriter().write(JSONUtils.convertToJson(getServices()));
                return;
            }
            String[] relevantPath = Arrays.copyOfRange(pathInfo, 1, pathInfo.length);
            String serviceIdString = relevantPath[0];

            UUID serviceId = UUID.fromString(serviceIdString);
            if(relevantPath.length == 1 && !relevantPath[0].isEmpty()) {
                if (getServiceIds().containsKey(serviceId)) {
                    JsonObject object = new JsonObject();
                    object.addProperty("serviceName", getServiceIds().get(serviceId));
                    res.getWriter().write(object.toString());
                }
            } else {
                try {
                    String path = Arrays.stream(relevantPath, 1, relevantPath.length).collect(Collectors.joining("/"));
                    Map<String, ServerHandler> handlerMap = Optional.ofNullable(handlers.get(serviceId))
                            .orElseThrow(() -> new ServletException("No such service ID: " + serviceId));
                    ServerHandler handler =  Optional.ofNullable(handlerMap.get("/" + path))
                            .orElseThrow(() -> new ServletException("No such path in service: /" + path));
                    handler.processRequest(req, res);
                } catch (ServletException e) {
                    log("Error while processing service", e);
                    res.setStatus(404);
                }

            }
        }
    }
}
