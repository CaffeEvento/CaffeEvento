package api.lib;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Created by chris on 7/16/16.
 */
public interface EmbeddedServletServer {
    void addService(String serviceName, UUID serviceId, String path, ServerHandler handler);
    void removeService(String serviceName);
    void removeService(UUID serviceId);
}
