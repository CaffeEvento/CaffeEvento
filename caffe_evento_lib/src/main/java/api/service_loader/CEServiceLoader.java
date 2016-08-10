package api.service_loader;

import api.services.Service;

/**
 * Created by chris on 8/10/16.
 */
public interface CEServiceLoader {
    Service loadService(CEFramework framework);
}
