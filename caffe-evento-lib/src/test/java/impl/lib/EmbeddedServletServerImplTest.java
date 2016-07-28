package impl.lib;

import api.lib.EmbeddedServletServer;
import impl.lib.servlet_server.EmbeddedServletServerImpl;
import impl.utils.JSONUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by chris on 7/16/16.
 */
public class EmbeddedServletServerImplTest {
    private EmbeddedServletServer instance;
    private int port = 2345;
    private Server server = new Server(port);

    @Before
    public void setUp() throws Exception {
        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/server");
        server.setHandler(servletContextHandler);
        instance = new EmbeddedServletServerImpl(servletContextHandler);
        server.start();
        Thread.sleep(100);
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(50);
        server.stop();
    }

    @Test
    public void testAddEndpoint() throws Exception {
        UUID serverId = UUID.randomUUID();
        String serviceName = "Test Service";
        instance.addService(serviceName, serverId, "/test", (req, res) -> {
            String value = req.getReader().lines().collect(Collectors.joining());
            assertEquals("test post data", value);
            res.getWriter().write("test response data");
        });

        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("http://localhost:"+port+"/server/"+serverId+"/test");
        post.setEntity(new StringEntity("test post data"));
        HttpResponse res = client.execute(post);
        HttpEntity entity = res.getEntity();
        if (entity != null) {
            try (InputStream inputStream = entity.getContent()) {
                String response = IOUtils.toString(inputStream, "UTF-8");
                assertEquals("test response data", response);
            }
        } else {
            fail();
        }

        Map<String, UUID> services = new HashMap<>();
        services.put(serviceName, serverId);

        HttpClient client1 = HttpClients.createDefault();
        HttpPost post1 = new HttpPost("http://localhost:"+port+"/server/services");
        post1.setEntity(new StringEntity("test services data"));
        HttpResponse res1 = client.execute(post1);
        HttpEntity entity1 = res1.getEntity();
        if (entity1 != null) {
            try (InputStream inputStream1 = entity1.getContent()) {
                String response1 = IOUtils.toString(inputStream1, "UTF-8");
                assertEquals(JSONUtils.convertToJson(services).toString(), response1);
            }
        } else {
            fail();
        }
    }

}