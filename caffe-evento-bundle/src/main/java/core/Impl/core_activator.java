package core.Impl;

import core.api.core_interface;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

import java.util.Properties;

/**
 * Created by eric on 7/29/16.
 */
public class core_activator implements BundleActivator{
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(core_interface.class.getName(),
                new core(),
                new Properties());
    }
    public void stop(BundleContext bundleContext) throws Exception {}
}
