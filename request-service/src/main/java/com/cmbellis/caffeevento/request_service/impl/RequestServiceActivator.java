package com.cmbellis.caffeevento.request_service.impl;

import com.cmbellis.caffeevento.lib.api.services.Service;
import com.cmbellis.caffeevento.request_service.impl.request_service.RequestServiceFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Created by chris on 8/11/16.
 */
public class RequestServiceActivator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        context.registerService(Service.class, RequestServiceFactory.getService(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {

    }
}
