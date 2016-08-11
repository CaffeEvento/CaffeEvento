package com.cmbellis.caffeevento.lib.impl.events.event_queue.event_queue_interface;

import com.cmbellis.caffeevento.lib.annotation.CEExport;
import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.events.EventHandler;
import com.cmbellis.caffeevento.lib.api.lib.EmbeddedServletServer;
import com.cmbellis.caffeevento.lib.api.utils.EventBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.cmbellis.caffeevento.lib.impl.utils.CEHttpUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by chris on 7/14/16.
 */
@CEExport
public class RemoteEventQueueInterface extends BufferedEventQueueInterface {
    private EmbeddedServletServer server;
    private UUID serverId = UUID.randomUUID();
    private UUID remoteServiceId;
    private String localEventReceiver;
    private boolean isConnected;
    private String remoteEventReceiver;
    private HttpClient client = HttpClients.createDefault();


    public RemoteEventQueueInterface(EmbeddedServletServer server, String serviceName, String localIp) {
        this.server = server;
        this.localEventReceiver = localIp + "/" + serverId.toString() + "/receiveEvent";
        server.addService(serviceName, serverId, "/receiveEvent", (req, res) -> Event.decodeEvent(req.getReader()).ifPresent(this::receiveEvent));
    }

    public UUID getServerId() {
        return serverId;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public synchronized void connectToServer(String serviceName, String remoteIp) throws IOException, RemoteEventQueueInterfaceException {
        if(isConnected) {
            disconnectFromServer();
        }

        // Connect to server to get remote service id
        JsonObject services = CEHttpUtils.doGetJson(remoteIp + "/services", Collections.emptyMap());

        String id = Optional.ofNullable(services.get(serviceName)).map(JsonElement::getAsString)
                .orElseThrow(() -> new RemoteEventQueueInterfaceException("No service with name: " + serviceName));
        remoteServiceId = UUID.fromString(id);
        remoteEventReceiver = remoteIp + "/" + remoteServiceId + "/receiveEvent";
        isConnected = true;

        // Add my event handlers to the remote client
        eventHandlers.forEach(this::addRemoteEventHandler);
    }

    public synchronized void disconnectFromServer() {
        // remove my event handlers from remote client
        eventHandlers.stream().map(EventHandler::getEventHandlerId).forEach(this::removeRemoteEventHandler);
        isConnected = false;
        remoteEventReceiver = null;
        remoteServiceId = null;
    }

    @Override
    public void addEventHandler(EventHandler handler) {
        // Add event handler locally
        super.addEventHandler(handler);
        addRemoteEventHandler(handler);
    }

    private void addRemoteEventHandler(EventHandler handler) {
        if (isConnected) {
            EventHandler remoteHandler = handler.getCopy();
            remoteHandler.addIpDestination(localEventReceiver);
            EventBuilder.create().name("ADD " + handler.getEventHandlerId())
                    .type("CREATE_EVENT_HANDLER")
                    .data("serverId", remoteServiceId.toString())
                    .data("eventHandlerDetails", handler.encodeToJson())
                    .build(this::sendEventToRemote);
        }
    }

    @Override
    public void removeEventHandler(EventHandler handler) {
        super.removeEventHandler(handler);
        removeRemoteEventHandler(handler.getEventHandlerId());
    }

    @Override
    public void removeEventHandler(UUID id) {
        super.removeEventHandler(id);
        removeRemoteEventHandler(id);
    }

    private void removeRemoteEventHandler(UUID id) {
        if (isConnected) {
            EventBuilder.create().name("REMOVE " + id)
                    .type("REMOVE_EVENT_HANDLER")
                    .data("serverId", remoteServiceId.toString())
                    .data("eventHandlerId", id.toString())
                    .build(this::sendEventToRemote);
        }
    }

    @Override
    public void receiveEvent(Event e) {
        super.receiveEvent(e);
        sendEventToRemote(e);
    }

    private void sendEventToRemote(Event e) {
        HttpPost post = new HttpPost(remoteEventReceiver);
        String eventJson = e.encodeEvent();
        try {
            CEHttpUtils.doPost(remoteEventReceiver, eventJson, ImmutableMap.of("content-type", "text/json"));
        } catch (IOException ex) {
            log.error("Unable to send event to remote: " + eventJson, ex);
        }

    }

    public class RemoteEventQueueInterfaceException extends Exception {
        public RemoteEventQueueInterfaceException(String message) {
            super(message);
        }
    }
}
