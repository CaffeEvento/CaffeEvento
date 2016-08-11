package com.cmbellis.caffeevento.lib.impl.events.event_queue.event_queue_interface;

import com.cmbellis.caffeevento.lib.api.events.Event;
import com.cmbellis.caffeevento.lib.api.events.EventHandler;
import com.cmbellis.caffeevento.lib.api.lib.EmbeddedServletServer;
import com.cmbellis.caffeevento.lib.api.lib.ServerHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.cmbellis.caffeevento.lib.impl.utils.CEHttpUtils;
import org.apache.http.HttpResponse;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.newCapture;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.*;

/**
 * Created by chris on 7/22/16.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
@PrepareForTest({CEHttpUtils.class, EmbeddedServletServer.class, UUID.class})
public class RemoteEventQueueInterfaceTest {
    @Mock EmbeddedServletServer mockServletServer;
    RemoteEventQueueInterface instance;
    UUID instanceId = UUID.randomUUID();

    String localIp = "123.233.233.101";
    String remoteIp = "123.123.123.123";

    @Before
    public void setUp() {
        mockServletServer.addService(eq("TEST_SERVICE"), anyObject(UUID.class), eq("/receiveEvent"), anyObject(ServerHandler.class));
        expectLastCall();
    }

    private void initializeEventQueueInterface() {
        instance = new RemoteEventQueueInterface(mockServletServer, "TEST_SERVICE", localIp);
    }

    @Test
    public void testConstructor() {
        replayAll();
        initializeEventQueueInterface();
        verifyAll();

        assertEquals(mockServletServer, Whitebox.getInternalState(instance, "server"));
        UUID serverId = instance.getServerId();
        assertEquals(localIp + "/" + serverId.toString() + "/receiveEvent",
                Whitebox.getInternalState(instance, "localEventReceiver"));
    }

    @Test
    public void testConnectToServerAddEventHandler() throws Exception{
        UUID remoteServiceId = UUID.randomUUID();
        JsonObject loginRetObject = new JsonObject();
        loginRetObject.add("Remote Service", new JsonPrimitive(remoteServiceId.toString()));

        mockStatic(CEHttpUtils.class);
        expect(CEHttpUtils.doGetJson(eq(remoteIp + "/services"), anyObject(Map.class)))
                .andReturn(loginRetObject);

        EventHandler handler =
                EventHandler.create().eventName("Test Event Handler").eventType("Test Event Type").build();

        Capture<String> receivedEvent = newCapture();
        expect(CEHttpUtils.doPost(eq(remoteIp + "/" + remoteServiceId.toString() + "/receiveEvent"),
                capture(receivedEvent), anyObject(Map.class))).andReturn(createMock(HttpResponse.class));

        replayAll();
        initializeEventQueueInterface();
        instance.connectToServer("Remote Service", remoteIp);
        instance.addEventHandler(handler);
        verifyAll();

        assertEquals(remoteServiceId, Whitebox.getInternalState(instance, "remoteServiceId"));
        assertEquals(remoteIp + "/" + remoteServiceId + "/receiveEvent", Whitebox.getInternalState(instance, "remoteEventReceiver"));
        assertEquals(true, instance.isConnected());
        assertTrue(receivedEvent.hasCaptured());
        Optional<Event> e = Event.decodeEvent(receivedEvent.getValue());
        assertTrue(e.isPresent());
        assertEquals("CREATE_EVENT_HANDLER", e.get().getEventType());
        assertEquals(remoteServiceId.toString(), e.get().getEventField("serverId"));

    }

    // TODO: Disconnect tests
    // TODO: Remove event handler tests
}