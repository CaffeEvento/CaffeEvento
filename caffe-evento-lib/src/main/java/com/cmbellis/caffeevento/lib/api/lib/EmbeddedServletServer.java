package com.cmbellis.caffeevento.lib.api.lib;

import java.util.UUID;

/**
 * Created by chris on 7/16/16.
 */
public interface EmbeddedServletServer {
    void addService(String serviceName, UUID serviceId, String path, ServerHandler handler);
    void removeService(String serviceName);
    void removeService(UUID serviceId);
}
