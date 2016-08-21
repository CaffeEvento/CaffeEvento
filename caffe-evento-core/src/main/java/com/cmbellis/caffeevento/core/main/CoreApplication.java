package com.cmbellis.caffeevento.core.main;

import com.cmbellis.caffeevento.lib.api.services.Service;
import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Created by chris on 8/10/16.
 */
public class CoreApplication {

//    private static ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();


//    public static void main(String[] argv) throws Exception
//    {
//        // Print welcome banner.
//        System.out.println("\nWelcome to CaffeEvento");
//        System.out.println("Powered by Apache Felix!");
//        System.out.println("======================\n");
//
//        Properties configProps = new Properties();
//        configProps.setProperty(AutoProcessor.AUTO_DEPLOY_DIR_PROPERTY, "./bundle");
//        configProps.setProperty(AutoProcessor.AUTO_DEPLOY_ACTION_PROPERTY, "install,update,start,uninstall");
//        configProps.setProperty(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
//        configProps.setProperty(Constants.FRAMEWORK_STORAGE, "cache");
//        configProps.setProperty(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
//                "com.cmbellis.caffeevento.lib.api.events," +
//                "com.cmbellis.caffeevento.lib.api.events.event_queue," +
//                "com.cmbellis.caffeevento.lib.api.events.event_queue.event_queue_interface," +
//                "com.cmbellis.caffeevento.lib.api.lib," +
//                "com.cmbellis.caffeevento.lib.api," +
//                );
//
//        try
//        {
//            final Framework framework = new FrameworkFactory().newFramework(configProps);
//            framework.init();
//            AutoProcessor.process(configProps, framework.getBundleContext());
//
////            framework.getBundleContext().addBundleListener(bundleEvent -> {
////                if(bundleEvent.getBundle() != null) {
////                    for(ServiceReference<?> serviceReference : bundleEvent.getBundle().getRegisteredServices()) {
////                        final ServiceObjects<?> serviceObjects =
////                                bundleEvent.getBundle().getBundleContext().getServiceObjects(serviceReference);
////                        Object s = serviceObjects.getService();
////                        if(s instanceof Service) {
////                            Service newService = (Service)s;
////                            System.out.println("A service event occurred on: " + newService.getClass());
////                        }
////                    }
////                }
////            });
//
//            framework.start();
//            framework.waitForStop(0);
//            System.exit(0);
//        }
//        catch (Exception ex)
//        {
//            System.err.println("Could not create framework: " + ex);
//            ex.printStackTrace();
//            System.exit(-1);
//        }
//    }
}
